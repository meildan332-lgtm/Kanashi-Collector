/*
 * Decompiled with CFR 0.152.
 */
package app.soopcollector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dialog;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

final class UpdateService {
    static final String CURRENT_VERSION = "1.2.0";
    static final String RELEASES_URL = "https://api.github.com/repos/meildan332-lgtm/Kanashi-Collector/releases/latest";
    static final String INSTALLER_ASSET_NAME = "Kanashi-Collector-Setup.exe";
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).followRedirects(HttpClient.Redirect.NORMAL).build();

    private UpdateService() {
    }

    static void check(Component parent, boolean notifyWhenCurrent) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(RELEASES_URL)).timeout(Duration.ofSeconds(15L)).header("Accept", "application/vnd.github+json").header("User-Agent", "Kanashi-Collector/1.2.0").GET().build();
        HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete((response, failure) -> SwingUtilities.invokeLater(() -> {
            block6: {
                if (failure != null || response == null || response.statusCode() != 200) {
                    if (notifyWhenCurrent) {
                        UpdateService.message(parent, "\uc5c5\ub370\uc774\ud2b8 \ud655\uc778 \uc2e4\ud328", "GitHub \ub9b4\ub9ac\uc2a4\ub97c \ud655\uc778\ud558\uc9c0 \ubabb\ud588\uc2b5\ub2c8\ub2e4.\n\uc778\ud130\ub137 \uc5f0\uacb0\uc744 \ud655\uc778\ud55c \ub4a4 \ub2e4\uc2dc \uc2dc\ub3c4\ud574 \uc8fc\uc138\uc694.", 2);
                    }
                    return;
                }
                try {
                    Release release = UpdateService.parseRelease((String)response.body());
                    if (UpdateService.compareVersions(release.version(), CURRENT_VERSION) <= 0) {
                        if (notifyWhenCurrent) {
                            UpdateService.message(parent, "\uc5c5\ub370\uc774\ud2b8 \ud655\uc778", "\ud604\uc7ac \ucd5c\uc2e0 \ubc84\uc804 " + UpdateService.displayVersion(CURRENT_VERSION) + "\uc744 \uc0ac\uc6a9\ud558\uace0 \uc788\uc2b5\ub2c8\ub2e4.", 1);
                        }
                        return;
                    }
                    UpdateService.showUpdatePrompt(parent, release);
                }
                catch (RuntimeException error) {
                    if (!notifyWhenCurrent) break block6;
                    UpdateService.message(parent, "\uc5c5\ub370\uc774\ud2b8 \ud655\uc778 \uc2e4\ud328", "\ub9b4\ub9ac\uc2a4 \uc815\ubcf4\ub97c \uc77d\uc9c0 \ubabb\ud588\uc2b5\ub2c8\ub2e4.", 2);
                }
            }
        }));
    }

    static Release parseRelease(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        String version = UpdateService.cleanVersion(root.get("tag_name").getAsString());
        String page = root.has("html_url") ? root.get("html_url").getAsString() : "https://github.com/meildan332-lgtm/Kanashi-Collector/releases";
        String notes = root.has("body") && !root.get("body").isJsonNull() ? root.get("body").getAsString() : "";
        String download = "";
        long size = 0L;
        JsonArray assets = root.has("assets") ? root.getAsJsonArray("assets") : new JsonArray();
        for (JsonElement value : assets) {
            JsonObject asset = value.getAsJsonObject();
            if (!INSTALLER_ASSET_NAME.equals(asset.get("name").getAsString())) continue;
            download = asset.get("browser_download_url").getAsString();
            size = asset.has("size") ? asset.get("size").getAsLong() : 0L;
            break;
        }
        return new Release(version, download, page, notes, size);
    }

    static int compareVersions(String left, String right) {
        int[] a = UpdateService.versionParts(left);
        int[] b = UpdateService.versionParts(right);
        for (int i = 0; i < Math.max(a.length, b.length); ++i) {
            int bv;
            int av = i < a.length ? a[i] : 0;
            int n = bv = i < b.length ? b[i] : 0;
            if (av == bv) continue;
            return Integer.compare(av, bv);
        }
        return 0;
    }

    private static int[] versionParts(String version) {
        String clean = UpdateService.cleanVersion(version).split("[-+]", 2)[0];
        return Arrays.stream(clean.split("\\.")).mapToInt(part -> {
            try {
                return Integer.parseInt(part.replaceAll("[^0-9]", ""));
            }
            catch (NumberFormatException ignored) {
                return 0;
            }
        }).toArray();
    }

    private static String cleanVersion(String version) {
        if (version == null) {
            return "0";
        }
        return version.trim().replaceFirst("^[vV]", "");
    }

    private static String displayVersion(String version) {
        return "v" + UpdateService.cleanVersion(version).replaceFirst("\\.0$", "");
    }

    private static void showUpdatePrompt(Component parent, Release release) {
        StringBuilder text = new StringBuilder("\uc0c8 \ubc84\uc804 v").append(release.version()).append("\uc774 \uc788\uc2b5\ub2c8\ub2e4.\n\ud604\uc7ac \ubc84\uc804: ").append(UpdateService.displayVersion(CURRENT_VERSION));
        if (!release.notes().isBlank()) {
            text.append("\n\n\uc5c5\ub370\uc774\ud2b8 \ub0b4\uc6a9\n").append((String)(release.notes().length() > 500 ? release.notes().substring(0, 500) + "\u2026" : release.notes()));
        }
        if (release.downloadUrl().isBlank()) {
            text.append("\n\n\uc124\uce58 \ud30c\uc77c\uc744 \ucc3e\uc9c0 \ubabb\ud574 \ub9b4\ub9ac\uc2a4 \ud398\uc774\uc9c0\ub97c \uc5fd\ub2c8\ub2e4.");
            if (JOptionPane.showConfirmDialog(parent, text, "\uc5c5\ub370\uc774\ud2b8", 2, 1) == 0) {
                UpdateService.openBrowser(release.pageUrl());
            }
            return;
        }
        text.append("\n\n\uc9c0\uae08 \ub0b4\ub824\ubc1b\uc544 \uc124\uce58\ud560\uae4c\uc694?");
        if (JOptionPane.showConfirmDialog(parent, text, "\uce74\ub098\uc2dc \uc218\uc9d1\ud568 \uc5c5\ub370\uc774\ud2b8", 0, 1) == 0) {
            UpdateService.downloadAndInstall(parent, release);
        }
    }

    private static void downloadAndInstall(Component parent, Release release) {
        JDialog progress = new JDialog(SwingUtilities.getWindowAncestor(parent), "\uc5c5\ub370\uc774\ud2b8 \ub2e4\uc6b4\ub85c\ub4dc", Dialog.ModalityType.APPLICATION_MODAL);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        JLabel label = new JLabel("\uc0c8 \ubc84\uc804 \uc124\uce58 \ud30c\uc77c\uc744 \ub0b4\ub824\ubc1b\ub294 \uc911\uc774\uc5d0\uc694", 0);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        panel.add((Component)label, "North");
        panel.add((Component)bar, "Center");
        progress.setContentPane(panel);
        progress.setSize(390, 125);
        progress.setResizable(false);
        progress.setLocationRelativeTo(parent);
        CompletableFuture.runAsync(() -> {
            try {
                Path temp = Files.createTempFile("Kanashi-Collector-Setup-", ".exe", new FileAttribute[0]);
                HttpRequest request = HttpRequest.newBuilder(URI.create(release.downloadUrl())).timeout(Duration.ofMinutes(5L)).header("User-Agent", "Kanashi-Collector/1.2.0").GET().build();
                Path partial = Path.of(temp.toString() + ".download", new String[0]);
                HttpResponse<Path> response = HTTP.send(request, HttpResponse.BodyHandlers.ofFile(partial));
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("HTTP " + response.statusCode());
                }
                long downloaded = Files.size(partial);
                if (downloaded == 0L || release.size() > 0L && downloaded != release.size()) {
                    throw new IOException("\ub2e4\uc6b4\ub85c\ub4dc \ud06c\uae30\uac00 \uc62c\ubc14\ub974\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4.");
                }
                Files.move(partial, temp, StandardCopyOption.REPLACE_EXISTING);
                new ProcessBuilder(temp.toString()).start();
                SwingUtilities.invokeLater(() -> {
                    progress.dispose();
                    System.exit(0);
                });
            }
            catch (Exception error) {
                SwingUtilities.invokeLater(() -> {
                    progress.dispose();
                    UpdateService.message(parent, "\uc5c5\ub370\uc774\ud2b8 \uc2e4\ud328", "\uc124\uce58 \ud30c\uc77c\uc744 \ub0b4\ub824\ubc1b\uc9c0 \ubabb\ud588\uc2b5\ub2c8\ub2e4.\n\ub9b4\ub9ac\uc2a4 \ud398\uc774\uc9c0\uc5d0\uc11c \uc9c1\uc811 \uc124\uce58\ud574 \uc8fc\uc138\uc694.", 0);
                    UpdateService.openBrowser(release.pageUrl());
                });
            }
        });
        progress.setVisible(true);
    }

    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void message(Component parent, String title, String body, int type) {
        JOptionPane.showMessageDialog(parent, body, title, type);
    }

    record Release(String version, String downloadUrl, String pageUrl, String notes, long size) {
    }
}
