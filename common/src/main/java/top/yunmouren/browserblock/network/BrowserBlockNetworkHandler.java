package top.yunmouren.browserblock.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public class BrowserBlockNetworkHandler {
    public static final ResourceLocation SET_BROWSER_URL_PACKET_ID = new ResourceLocation(MOD_ID, "set_browser_url");

    public static void registerC2SReceivers() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SET_BROWSER_URL_PACKET_ID, (buf, context) -> {
            PacketSetBrowserUrl pkt = new PacketSetBrowserUrl(buf);
            context.queue(() -> PacketSetBrowserUrl.handle(pkt, context));
        });
    }

    public static void sendToServer(BlockPos pos, String url, double volume) {
        PacketSetBrowserUrl pkt = new PacketSetBrowserUrl(pos, url, volume);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        pkt.encode(buf);

        NetworkManager.sendToServer(SET_BROWSER_URL_PACKET_ID, buf);
    }
}