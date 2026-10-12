package app.soopcollector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** SOOP login and Windows DPAPI-backed session persistence. */
final class SoopSessionManager {
    private static final URI LOGIN_URI = URI.create("https://login.sooplive.co.kr/app/LoginAction.php");
    private static final Path SESSION_FILE = Path.of(System.getProperty("user.home"), ".soop-collector", "session.dat");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final Gson GSON = new Gson();
    private static final String PROTECT_SCRIPT =
            "Add-Type -AssemblyName System.Security;"
                    + "$b=[Convert]::FromBase64String([Console]::In.ReadToEnd());"
                    + "$p=[Security.Cryptography.ProtectedData]::Protect($b,$null,[Security.Cryptography.DataProtectionScope]::CurrentUser);"
                    + "[Console]::Out.Write([Convert]::ToBase64String($p))";
    private static final String UNPROTECT_SCRIPT =
            "Add-Type -AssemblyName System.Security;"
                    + "$b=[Convert]::FromBase64String([Console]::In.ReadToEnd());"
                    + "$p=[Security.Cryptography.ProtectedData]::Unprotect($b,$null,[Security.Cryptography.DataProtectionScope]::CurrentUser);"
                    + "[Console]::Out.Write([Convert]::ToBase64String($p))";

    private SoopSessionManager() {}

    static Session login(String userId, char[] password) throws IOException, InterruptedException {
        String passwordText = new String(password);
        try {
            String body = "szWork=login&szType=json&szUid=" + encode(userId)
                    + "&szPassword=" + encode(passwordText);
            HttpRequest request = HttpRequest.newBuilder(LOGIN_URI)
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("User-Agent", "Kanashi-Collector/1.1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("로그인 서버 응답 오류 (HTTP " + response.statusCode() + ")");
            }
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            int result = json.has("RESULT") && !json.get("RESULT").isJsonNull()
                    ? json.get("RESULT").getAsInt() : 0;
            if (result != 1) {
                String reason = json.has("REASON") && !json.get("REASON").isJsonNull()
                        ? json.get("REASON").getAsString() : "아이디 또는 비밀번호를 확인해 주세요.";
                throw new AuthenticationException(reason);
            }
            Map<String, String> cookies = parseCookies(response.headers().allValues("set-cookie"));
            if (cookies.getOrDefault("AuthTicket", "").isBlank()) {
                throw new AuthenticationException("로그인 세션 쿠키를 받지 못했습니다.");
            }
            Session session = new Session(userId, cookies);
            save(session);
            return session;
        } finally {
            passwordText = null;
            java.util.Arrays.fill(password, '\0');
        }
    }

    static Session load() {
        try {
            if (!Files.isRegularFile(SESSION_FILE)) return null;
            byte[] encrypted = Files.readAllBytes(SESSION_FILE);
            byte[] plain = dpapi(encrypted, false);
            Session session = GSON.fromJson(new String(plain, StandardCharsets.UTF_8), Session.class);
            java.util.Arrays.fill(plain, (byte) 0);
            return session != null && session.isUsable() ? session : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    static void clear() {
        try {
            Files.deleteIfExists(SESSION_FILE);
        } catch (IOException ignored) {
            // A failed deletion must not expose the session or crash the collector.
        }
    }

    private static void save(Session session) throws IOException, InterruptedException {
        Files.createDirectories(SESSION_FILE.getParent());
        byte[] plain = GSON.toJson(session).getBytes(StandardCharsets.UTF_8);
        try {
            byte[] encrypted = dpapi(plain, true);
            Files.write(SESSION_FILE, encrypted, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } finally {
            java.util.Arrays.fill(plain, (byte) 0);
        }
    }

    private static byte[] dpapi(byte[] input, boolean protect) throws IOException, InterruptedException {
        String script = protect ? PROTECT_SCRIPT : UNPROTECT_SCRIPT;
        String encodedScript = java.util.Base64.getEncoder()
                .encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
        Process process = new ProcessBuilder("powershell.exe", "-NoLogo", "-NoProfile", "-NonInteractive",
                "-OutputFormat", "Text", "-EncodedCommand", encodedScript).start();
        try (var output = process.getOutputStream()) {
            output.write(java.util.Base64.getEncoder().encode(input));
        }
        byte[] result = process.getInputStream().readAllBytes();
        byte[] error = process.getErrorStream().readAllBytes();
        if (process.waitFor() != 0) {
            throw new IOException("Windows DPAPI 처리에 실패했습니다: "
                    + new String(error, StandardCharsets.UTF_8).trim());
        }
        try {
            return java.util.Base64.getDecoder().decode(new String(result, StandardCharsets.US_ASCII).trim());
        } catch (IllegalArgumentException e) {
            throw new IOException("Windows DPAPI 응답을 읽지 못했습니다.", e);
        }
    }

    private static Map<String, String> parseCookies(List<String> headers) {
        Map<String, String> cookies = new LinkedHashMap<>();
        for (String header : headers) {
            try {
                for (HttpCookie cookie : HttpCookie.parse(header)) {
                    cookies.put(cookie.getName(), cookie.getValue());
                }
            } catch (IllegalArgumentException ignored) {
                // Never include a cookie header in logs or exceptions.
            }
        }
        return cookies;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    static final class Session {
        String userId;
        Map<String, String> cookies;

        Session() {}

        Session(String userId, Map<String, String> cookies) {
            this.userId = userId;
            this.cookies = new LinkedHashMap<>(cookies);
        }

        String userId() { return userId == null ? "" : userId; }

        boolean isUsable() {
            return cookies != null && !cookies.getOrDefault("AuthTicket", "").isBlank();
        }

        String cookieHeader() {
            if (!isUsable()) return null;
            StringBuilder result = new StringBuilder();
            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                if (entry.getValue() == null || entry.getValue().isBlank()) continue;
                if (result.length() > 0) result.append("; ");
                result.append(entry.getKey()).append('=').append(entry.getValue());
            }
            return result.toString();
        }

        @Override public String toString() {
            return "Session[userId=" + userId() + ", cookies=<redacted>]";
        }
    }

    static final class AuthenticationException extends IOException {
        AuthenticationException(String message) { super(message); }
    }
}
