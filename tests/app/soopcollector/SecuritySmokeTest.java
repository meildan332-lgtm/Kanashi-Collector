package app.soopcollector;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;
import zzik2.soop4j.api.SoopLive;

public final class SecuritySmokeTest {
    public static void main(String[] args) throws Exception {
        testAuthenticationClassification();
        testSessionRedaction();
        testDpapiRoundTrip();
        testChatViewerClassification();
        testFivePinballRules();
        testPinDonationCollectionSwitch();
        testRoomDonationCollection();
        testReverseDonationCollection();
        testTimerFormatting();
        testPinChatCollectionRules();
        testDefaultCollectionState();
        testTabGuides();
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

    private static void testChatViewerClassification() {
        require(Main.classifyChatViewer("16|0", "-1") == Main.ChatViewerType.NORMAL,
                "guest chat must be classified as normal");
        require(Main.classifyChatViewer("32|0", "-1") == Main.ChatViewerType.FAN,
                "fan-club chat must be classified as fan");
        require(Main.classifyChatViewer("32|0", "3") == Main.ChatViewerType.SUBSCRIBER,
                "subscriber must take priority over fan-club status");
        require(Main.classifyChatViewer("32800|0", "12") == Main.ChatViewerType.TOP_FAN,
                "top fan must take priority over other chat statuses");
    }

    private static void testFivePinballRules() {
        require(Main.pinballBalls(200, List.of(20, 50)) == 10,
                "rule 1 must take priority when multiple rules match");
        require(Main.pinballBalls(200, List.of(50, 20)) == 4,
                "changing rule 1 must change the default multiple calculation");
        require(Main.pinballBalls(150, List.of(20, 50)) == 3,
                "a later rule must apply when rule 1 does not divide the donation");
        require(Main.pinballBalls(200, List.of(20, 40, 50, 100, 200)) == 10,
                "no more than the first matching rule may be applied");
        require(Main.pinballBalls(12, List.of()) == 0,
                "empty pinball rules must not collect balls");
        require(Main.challengeBalls(200) == 4,
                "challenge mode must calculate one ball per 50 donation units");
        require(Main.challengeBalls(125) == 2,
                "challenge mode must keep one ball for each complete group of 50");
    }

    private static void testPinDonationCollectionSwitch() {
        int balls = Main.pinballBalls(100, List.of(20));
        require(balls == 5, "a 100-unit donation must create five balls for a 20-unit rule");
        require(Main.shouldCollectPinDonation(true, true, balls),
                "enabled donation collection must accept a matching donation");
        require(!Main.shouldCollectPinDonation(false, true, balls),
                "donation collection OFF must block a matching donation");
        require(!Main.shouldCollectPinDonation(true, false, balls),
                "a disabled donation source must be blocked");
        require(!Main.shouldCollectPinDonation(true, true, 0),
                "a donation that matches no rule must be blocked");
    }

    private static void testRoomDonationCollection() {
        List<Integer> rules = List.of(100, 500, 1000);
        require(Main.shouldCollectRoomDonation(true, 500, rules),
                "room collection must accept an exact registered amount");
        require(!Main.shouldCollectRoomDonation(true, 501, rules),
                "room collection must reject an unregistered amount");
        require(!Main.shouldCollectRoomDonation(false, 500, rules),
                "a disabled room donation source must be blocked");
    }

    private static void testReverseDonationCollection() {
        Main.ReverseRule low = new Main.ReverseRule(100, 10.0);
        Main.ReverseRule high = new Main.ReverseRule(500, 25.0);
        Main.ReverseRule matched = Main.matchingReverseRule(600, List.of(low, high));
        require(matched == high, "reverse collection must choose the highest matching minimum");
        require((int)Math.floor(600 * matched.percent / 100.0) == 150,
                "reverse collection percentage calculation is incorrect");
        require(Main.matchingReverseRule(99, List.of(low, high)) == null,
                "reverse collection must reject donations below every minimum");
    }

    private static void testTimerFormatting() {
        require("01:01:01".equals(Main.timerText(3661)), "timer must format hours, minutes and seconds");
        require("00:00:00".equals(Main.timerText(-1)), "timer must never display a negative value");
        require("1234:00:00".equals(Main.timerText(1234L * 3600L)),
                "timer hours must not be limited to two digits");
    }

    private static void testPinChatCollectionRules() {
        require(Main.shouldCollectPinChat(true, false, true, false, false),
                "an enabled first chat must be collected");
        require(!Main.shouldCollectPinChat(false, false, true, true, false),
                "chat collection OFF must block chats");
        require(!Main.shouldCollectPinChat(true, true, true, true, false),
                "the game-name chat consumed by a donation must not create another chat ball");
        require(!Main.shouldCollectPinChat(true, false, false, true, false),
                "an unchecked viewer type must be blocked");
        require(!Main.shouldCollectPinChat(true, false, true, false, true),
                "duplicate OFF must allow only one ball per user id");
        require(Main.shouldCollectPinChat(true, false, true, true, true),
                "duplicate ON must allow another ball for the same user id");
    }

    private static void testDefaultCollectionState() {
        Main.State state = new Main.State().normalize();
        require(state.roomStar && state.roomAd && state.roomChallenge,
                "all room donation sources must default to enabled");
        require(state.reverseStar && state.reverseAd && state.reverseChallenge,
                "all reverse donation sources must default to enabled");
        require(state.pinDonationEnabled && state.pinStar && state.pinAd && state.pinChallenge,
                "pin donation collection and all donation sources must default to enabled");
        require(!state.pinChatEnabled && !state.pinChatDuplicateEnabled,
                "chat collection and duplicate collection must default to off");
        require(state.pinChatNormal && state.pinChatFan && state.pinChatSubscriber && state.pinChatTopFan,
                "all chat viewer types must default to enabled");
    }

    private static void testTabGuides() {
        for (String tab : List.of("room", "reverse", "pin", "nashiball", "timer")) {
            require(!Main.guideTitle(tab).isBlank(), "guide title is missing for " + tab);
            require(Main.guideText(tab).length() > 80, "guide content is missing for " + tab);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
