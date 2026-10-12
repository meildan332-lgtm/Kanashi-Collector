package zzik2.soop4j.api;

import com.google.gson.JsonObject;
import zzik2.soop4j.constant.SoopUrls;
import zzik2.soop4j.exception.AuthenticationRequiredException;
import zzik2.soop4j.exception.SoopException;
import zzik2.soop4j.exception.SoopApiException;
import zzik2.soop4j.internal.Futures;
import zzik2.soop4j.internal.Inputs;
import zzik2.soop4j.http.SoopHttpClient;
import zzik2.soop4j.model.live.LiveChannel;
import zzik2.soop4j.model.live.LiveDetail;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SoopLive {
    private static final String THUMBNAIL_URL_FORMAT = "https://liveimg.sooplive.co.kr/m/%s";
    private final SoopHttpClient httpClient;
    private final String baseUrl;
    private final SoopChannel channelApi;
    private final String cookieHeader;

    public SoopLive(SoopHttpClient httpClient) { this(httpClient, SoopUrls.LIVE_BASE_URL, (String)null); }
    public SoopLive(SoopHttpClient httpClient, String baseUrl) { this(httpClient, baseUrl, (String)null); }

    /** Creates a live API whose discovery requests use the supplied SOOP session cookie. */
    public SoopLive(SoopHttpClient httpClient, String baseUrl, String cookieHeader) {
        this(httpClient, baseUrl, new SoopChannel(httpClient), cookieHeader);
    }

    public SoopLive(SoopHttpClient httpClient, String baseUrl, SoopChannel channelApi) {
        this(httpClient, baseUrl, channelApi, null);
    }

    SoopLive(SoopHttpClient httpClient, String baseUrl, SoopChannel channelApi, String cookieHeader) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.channelApi = channelApi;
        this.cookieHeader = cookieHeader;
    }

    public LiveDetail detail(String streamerId) { return Futures.await(detailAsync(streamerId)); }

    private CompletableFuture<LiveChannel> channelAsync(String streamerId) {
        try {
            Inputs.streamerId(streamerId);
            Map<String, String> formData = new HashMap<>();
            formData.put("bid", streamerId);
            formData.put("type", "live");
            formData.put("pwd", "");
            formData.put("player_type", "html5");
            formData.put("stream_type", "common");
            formData.put("quality", "HD");
            formData.put("mode", "landing");
            formData.put("from_api", "0");
            formData.put("is_revive", "false");
            formData.put("confirm_adult", "false");

            String url = baseUrl + "/afreeca/player_live_api.php?bjid=" + streamerId;
            return Futures.map(httpClient.postFormAsync(url, formData, cookieHeader), response -> {
                try {
                    JsonObject root = response;
                    if (!root.has("CHANNEL") || !root.get("CHANNEL").isJsonObject()) {
                        int result = integer(root, "RESULT", Integer.MIN_VALUE);
                        String reason = string(root, "REASON");
                        if (isAuthenticationFailure(result, reason)) {
                            throw new AuthenticationRequiredException(streamerId, result, reason);
                        }
                        throw new SoopException("API 응답에 CHANNEL 정보가 없습니다: " + streamerId);
                    }
                    LiveChannel channel = httpClient.getGson().fromJson(root.get("CHANNEL"), LiveChannel.class);
                    if (channel.getResult() != 0 && channel.getResult() != 1) {
                        if (isAuthenticationFailure(channel.getResult(), channel.getReason())) {
                            throw new AuthenticationRequiredException(streamerId, channel.getResult(), channel.getReason());
                        }
                        throw new SoopApiException(streamerId, channel.getResult());
                    }
                    return channel;
                } catch (SoopException e) { throw e; }
                catch (Exception e) { throw new SoopException("라이브 응답 파싱 실패: " + streamerId, e); }
            });
        } catch (Exception e) { return CompletableFuture.failedFuture(e); }
    }

    private static boolean isAuthenticationFailure(int result, String reason) {
        if (result == -6) return true;
        String text = reason == null ? "" : reason.toLowerCase(Locale.ROOT);
        return text.contains("로그인") || text.contains("login") || text.contains("구독")
                || text.contains("subscriber") || text.contains("성인") || text.contains("adult")
                || text.contains("인증") || text.contains("권한");
    }

    private static int integer(JsonObject json, String name, int fallback) {
        try { return json.has(name) && !json.get(name).isJsonNull() ? json.get(name).getAsInt() : fallback; }
        catch (RuntimeException ignored) { return fallback; }
    }

    private static String string(JsonObject json, String name) {
        try { return json.has(name) && !json.get(name).isJsonNull() ? json.get(name).getAsString() : ""; }
        catch (RuntimeException ignored) { return ""; }
    }

    public CompletableFuture<LiveDetail> detailAsync(String streamerId) {
        return Futures.compose(channelAsync(streamerId), channel -> channel.isOnline()
                ? Futures.map(getViewerCountAsync(streamerId), count -> LiveDetail.fromChannel(channel, count))
                : CompletableFuture.completedFuture(LiveDetail.fromChannel(channel)));
    }

    public boolean isOnline(String streamerId) { return Futures.await(isOnlineAsync(streamerId)); }
    public CompletableFuture<Boolean> isOnlineAsync(String streamerId) {
        return Futures.map(channelAsync(streamerId), LiveChannel::isOnline);
    }
    public int getViewerCount(String streamerId) { return Futures.await(getViewerCountAsync(streamerId)); }
    public CompletableFuture<Integer> getViewerCountAsync(String streamerId) {
        return Futures.map(channelApi.stationAsync(streamerId), station -> station.isOnline()
                ? station.getBroad().getCurrentSumViewer() : 0);
    }
    public String getThumbnailUrl(String streamerId) {
        return String.format(THUMBNAIL_URL_FORMAT, Inputs.streamerId(streamerId));
    }
}
