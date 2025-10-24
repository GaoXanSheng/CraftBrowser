package top.yunmouren.craftbrowser.server.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import top.yunmouren.craftbrowser.client.network.ClientEnumeration;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

/**
 * 通用浏览器网络处理器
 * 使用 Architectury NetworkManager 实现跨平台网络通信
 */
public class BrowserNetworkHandler {

    private static final BrowserNetworkHandler INSTANCE = new BrowserNetworkHandler();

    public static BrowserNetworkHandler getInstance() {
        return INSTANCE;
    }

    // 定义数据包的标识符 (ResourceLocation)
    public static final ResourceLocation BROWSER_PACKET_ID = new ResourceLocation(MOD_ID, "browser");

    /**
     * 注册服务器到客户端的数据包接收器
     * 在客户端调用
     */
    public void registerClientReceiver() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, BROWSER_PACKET_ID, (buf, context) -> {
            BrowserPacket pkt = new BrowserPacket(buf);
            context.queue(() -> BrowserPacket.handle(pkt, context));
        });
    }

    /**
     * 向指定玩家发送打开开发者工具的消息
     * @param player 目标玩家
     */
    public void sendOpenDev(ServerPlayer player) {
        sendToPlayer(player, "OPEN_DEV", "");
    }

    /**
     * 向指定玩家发送加载URL的消息
     * @param player 目标玩家
     * @param url 要加载的URL
     */
    public void sendLoadUrl(ServerPlayer player, String url) {
        sendToPlayer(player, "LOAD_URL", url);
    }

    /**
     * 向指定玩家发送消息
     */
    private void sendToPlayer(ServerPlayer player, String type, String body) {
        BrowserPacket packet = new BrowserPacket(type, body);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);

        NetworkManager.sendToPlayer(player, BROWSER_PACKET_ID, buf);
    }

    /**
     * 浏览器网络消息包
     */
    public record BrowserPacket(String type, String body) {

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(type);
            buf.writeUtf(body != null ? body : "");
        }

        public BrowserPacket(FriendlyByteBuf buf) {
            this(buf.readUtf(), buf.readUtf());
        }

        public static void handle(BrowserPacket pkt, NetworkManager.PacketContext context) {
            // 客户端执行
            new ClientEnumeration(pkt.type, pkt.body);
        }
    }
}

