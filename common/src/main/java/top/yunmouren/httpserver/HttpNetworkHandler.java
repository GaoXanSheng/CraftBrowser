package top.yunmouren.httpserver;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.yunmouren.craftbrowser.client.config.Config;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public class HttpNetworkHandler {

    // 1. 定义数据包的标识符 (ResourceLocation)
    public static final ResourceLocation HTTP_REQUEST_PACKET_ID = new ResourceLocation(MOD_ID, "http_request");
    public static final ResourceLocation HTTP_RESPONSE_PACKET_ID = new ResourceLocation(MOD_ID, "http_response");

    private static final ConcurrentHashMap<UUID, CompletableFuture<String>> PENDING_REQUESTS = new ConcurrentHashMap<>();

    public static void registerC2SReceivers() {
        // 只注册 C2S (Client to Server) 的数据包接收器
        // 因为服务器需要监听来自客户端的请求
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, HTTP_REQUEST_PACKET_ID, (buf, context) -> {
            HttpRequestPacket pkt = new HttpRequestPacket(buf);
            context.queue(() -> HttpRequestPacket.handle(pkt, context));
        });
    }
    public static void registerS2CReceivers() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, HTTP_RESPONSE_PACKET_ID, (buf, context) -> {
            HttpResponsePacket pkt = new HttpResponsePacket(buf);
            context.queue(() -> HttpResponsePacket.handle(pkt, context));
        });
    }


    public static void sendToServer(String data, CompletableFuture<String> future) {
        UUID requestId = UUID.randomUUID();
        PENDING_REQUESTS.put(requestId, future);

        HttpRequestPacket packet = new HttpRequestPacket(requestId, data);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);

        // 4. 发送数据包到服务器
        NetworkManager.sendToServer(HTTP_REQUEST_PACKET_ID, buf);
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
            e.printStackTrace();
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


    public record HttpRequestPacket(UUID requestId, String data) {

        public void encode(FriendlyByteBuf buf) {
            buf.writeUUID(requestId);
            buf.writeUtf(data);
        }

        public HttpRequestPacket(FriendlyByteBuf buf) {
            this(buf.readUUID(), buf.readUtf(32767));
        }

        public static void handle(HttpRequestPacket pkt, NetworkManager.PacketContext context) {
            // 服务器端执行
            String httpResponse = sendHttpToExternal(pkt.data);

            HttpResponsePacket reply = new HttpResponsePacket(pkt.requestId, httpResponse);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            reply.encode(buf);

            // --- 修改开始 ---
            // 获取玩家对象
            net.minecraft.world.entity.player.Player player = context.getPlayer();

            // 检查玩家是否是 ServerPlayer 的实例，并进行类型转换
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                // 使用转换后的 serverPlayer 对象发送数据包
                NetworkManager.sendToPlayer(serverPlayer, HTTP_RESPONSE_PACKET_ID, buf);
            }
        }
    }

    public static class HttpResponsePacket {
        private final UUID requestId;
        private final String responseData;

        public HttpResponsePacket(UUID requestId, String responseData) {
            this.requestId = requestId;
            this.responseData = responseData;
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeUUID(requestId);
            buf.writeUtf(responseData);
        }

        // 从FriendlyByteBuf解码的构造函数
        public HttpResponsePacket(FriendlyByteBuf buf) {
            this.requestId = buf.readUUID();
            this.responseData = buf.readUtf(32767);
        }

        public static void handle(HttpResponsePacket pkt, NetworkManager.PacketContext context) {
            // 客户端执行
            CompletableFuture<String> future = HttpNetworkHandler.getPendingFuture(pkt.requestId);

            if (future != null) {
                future.complete(pkt.responseData);
            }
        }
    }
}