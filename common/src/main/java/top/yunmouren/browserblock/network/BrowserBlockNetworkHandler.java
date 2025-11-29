package top.yunmouren.browserblock.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public class BrowserBlockNetworkHandler {
    // 定义数据包 ID
    public static final ResourceLocation SET_BROWSER_URL_PACKET_ID = new ResourceLocation(MOD_ID, "set_browser_url");

    public static void registerC2SReceivers() {
        // 注册 C2S (Client to Server) 接收器
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SET_BROWSER_URL_PACKET_ID, (buf, context) -> {
            PacketSetBrowserUrl pkt = new PacketSetBrowserUrl(buf);
            // 将处理逻辑加入主线程队列
            context.queue(() -> PacketSetBrowserUrl.handle(pkt, context));
        });
    }

    // 发送数据包到服务器的辅助方法
    public static void sendToServer(BlockPos pos, String url) {
        PacketSetBrowserUrl pkt = new PacketSetBrowserUrl(pos, url);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        pkt.encode(buf);

        NetworkManager.sendToServer(SET_BROWSER_URL_PACKET_ID, buf);
    }
}