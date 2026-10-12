package app.soopcollector;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import zzik2.soop4j.api.SoopLive;

public final class SecuritySmokeTest {
    public static void main(String[] args) throws Exception {
        testAuthenticationClassification();
        testSessionRedaction();
        testDpapiRoundTrip();
        System.out.println("SecuritySmokeTest: OK");
    }

    private static void testAuthenticationClassification() throws Exception {
        Method method = SoopLive.class.getDeclaredMethod("isAuthenticationFailure", int.class, String.class);
        method.setAccessible(true);
        require((Boolean)method.invoke(null, -6, ""), "RESULT=-6 must require authentication");
        require((Boolean)method.invoke(null, -3, "구독자 전용 방송"), "subscriber-only reason must require authentication");
        require(!(Boolean)method.invoke(null, -3, "temporary server failure"), "server failure must not be authentication");
    }

    private static void testSessionRedaction() {
        SoopSessionManager.Session session = new SoopSessionManager.Session(
                "tester", Map.of("AuthTicket", "top-secret", "_au", "also-secret"));
        String text = session.toString();
        require(!text.contains("top-secret") && !text.contains("also-secret"), "session string leaked a cookie");
        require(session.cookieHeader().contains("AuthTicket=top-secret"), "cookie header is incomplete");
    }

    private static void testDpapiRoundTrip() throws Exception {
        Method method = SoopSessionManager.class.getDeclaredMethod("dpapi", byte[].class, boolean.class);
        method.setAccessible(true);
        byte[] plain = "kanashi-dpapi-smoke".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = (byte[])method.invoke(null, plain, true);
        byte[] decrypted = (byte[])method.invoke(null, encrypted, false);
        require(java.util.Arrays.equals(plain, decrypted), "DPAPI round-trip failed");
        require(!java.util.Arrays.equals(plain, encrypted), "DPAPI did not encrypt the payload");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
