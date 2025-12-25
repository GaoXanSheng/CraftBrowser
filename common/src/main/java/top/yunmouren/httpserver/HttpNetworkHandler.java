package top.yunmouren.httpserver;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.client.config.Config;
import top.yunmouren.craftbrowser.server.network.packet.HttpRequestPacket;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public class HttpNetworkHandler {


    public static String sendHttpToExternal(String data) {
        try {
            URL url = new URL(Config.CLIENT.externalApiUrl.get());
            final HttpURLConnection con = getHttpURLConnection(data, url);

            java.io.InputStream is;
            int responseCode = con.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                is = con.getInputStream();
            } else {
                is = con.getErrorStream();
                if (is == null) {
                    return "HTTP request failed with response code: " + responseCode;
                }
            }

            byte[] bytes = is.readAllBytes();
            String response = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            is.close();

            return response;

        } catch (Exception e) {
            Craftbrowser.LOGGER.error("HTTP request failed", e);
            return "HTTP request failed: " + e.getMessage();
        }
    }

    @NotNull
    private static HttpURLConnection getHttpURLConnection(String data, URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        byte[] postData = data.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setRequestProperty("Content-Length", String.valueOf(postData.length));

        try (java.io.OutputStream os = con.getOutputStream()) {
            os.write(postData);
            os.flush();
        }
        return con;
    }

    private static final ConcurrentHashMap<UUID, CompletableFuture<String>> PENDING_REQUESTS = new ConcurrentHashMap<>();
    public static void sendToServer(String data, CompletableFuture<String> future) {
        UUID requestId = UUID.randomUUID();
        PENDING_REQUESTS.put(requestId, future);
        HttpRequestPacket packet = new HttpRequestPacket(requestId, data);
        NetworkManager.sendToServer(packet);
    }

    public static CompletableFuture<String> getPendingFuture(UUID requestId) {
        return PENDING_REQUESTS.remove(requestId);
    }
}