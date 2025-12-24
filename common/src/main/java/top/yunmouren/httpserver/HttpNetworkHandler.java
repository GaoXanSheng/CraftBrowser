package top.yunmouren.httpserver;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.client.config.Config;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public class HttpNetworkHandler {

    public static final ResourceLocation HTTP_REQUEST_PACKET_ID =  ResourceLocation.fromNamespaceAndPath(MOD_ID, "http_request");
    public static final ResourceLocation HTTP_RESPONSE_PACKET_ID =  ResourceLocation.fromNamespaceAndPath(MOD_ID, "http_response");

    private static final ConcurrentHashMap<UUID, CompletableFuture<String>> PENDING_REQUESTS = new ConcurrentHashMap<>();



    public record HttpRequestPayload(
            UUID requestId,
            String data
    ) implements CustomPacketPayload {

        public static final Type<HttpRequestPayload> TYPE =
                new Type<>(HTTP_REQUEST_PACKET_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, HttpRequestPayload> CODEC =
                StreamCodec.of(
                        HttpRequestPayload::encode,
                        HttpRequestPayload::decode
                );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(RegistryFriendlyByteBuf buf, HttpRequestPayload pkt) {
            buf.writeUUID(pkt.requestId);
            buf.writeUtf(pkt.data);
        }

        private static HttpRequestPayload decode(RegistryFriendlyByteBuf buf) {
            return new HttpRequestPayload(buf.readUUID(), buf.readUtf(32767));
        }
    }

    public record HttpResponsePayload(
            UUID requestId,
            String responseData
    ) implements CustomPacketPayload {

        public static final Type<HttpResponsePayload> TYPE =
                new Type<>(HTTP_RESPONSE_PACKET_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, HttpResponsePayload> CODEC =
                StreamCodec.of(
                        HttpResponsePayload::encode,
                        HttpResponsePayload::decode
                );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(RegistryFriendlyByteBuf buf, HttpResponsePayload pkt) {
            buf.writeUUID(pkt.requestId);
            buf.writeUtf(pkt.responseData);
        }

        private static HttpResponsePayload decode(RegistryFriendlyByteBuf buf) {
            return new HttpResponsePayload(buf.readUUID(), buf.readUtf(32767));
        }
    }

    public static void registerC2SReceivers() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                HttpRequestPayload.TYPE,
                HttpRequestPayload.CODEC,
                (payload, context) -> context.queue(() -> handleHttpRequest(payload, context))
        );
    }

    public static void registerS2CReceivers() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                HttpResponsePayload.TYPE,
                HttpResponsePayload.CODEC,
                (payload, context) -> context.queue(() -> handleHttpResponse(payload, context))
        );
    }

    public static void sendToServer(String data, CompletableFuture<String> future) {
        UUID requestId = UUID.randomUUID();
        PENDING_REQUESTS.put(requestId, future);
        NetworkManager.sendToServer(new HttpRequestPayload(requestId, data));
    }

    public static CompletableFuture<String> getPendingFuture(UUID requestId) {
        return PENDING_REQUESTS.remove(requestId);
    }

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

    private static void handleHttpRequest(HttpRequestPayload payload, NetworkManager.PacketContext context) {
        String httpResponse = sendHttpToExternal(payload.data());

        HttpResponsePayload reply = new HttpResponsePayload(payload.requestId(), httpResponse);

        net.minecraft.world.entity.player.Player player = context.getPlayer();
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            NetworkManager.sendToPlayer(serverPlayer, reply);
        }
    }

    private static void handleHttpResponse(HttpResponsePayload payload, NetworkManager.PacketContext context) {
        CompletableFuture<String> future = getPendingFuture(payload.requestId());

        if (future != null) {
            future.complete(payload.responseData());
        }
    }
}