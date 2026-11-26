package top.yunmouren.browserblock.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public class BrowserBlockNetworkHandler {
    public static final ResourceLocation SET_BROWSER_URL_PACKET_ID = new ResourceLocation(MOD_ID, "set_browser_url");
    public static final ResourceLocation REQUEST_BROWSER_URL_PACKET_ID = new ResourceLocation(MOD_ID, "request_browser_url");
    public static final ResourceLocation BROWSER_STATE_PACKET_ID = new ResourceLocation(MOD_ID, "browser_state");

    public static void registerC2SReceivers() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SET_BROWSER_URL_PACKET_ID, (buf, context) -> {
            PacketSetBrowserUrl pkt = new PacketSetBrowserUrl(buf);
            context.queue(() -> PacketSetBrowserUrl.handle(pkt, context));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, REQUEST_BROWSER_URL_PACKET_ID, (buf, context) -> {
            PacketRequestBrowserUrl pkt = new PacketRequestBrowserUrl(buf);
            context.queue(() -> PacketRequestBrowserUrl.handle(pkt, context));
        });
    }

    public static void registerS2CReceivers() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, BROWSER_STATE_PACKET_ID, (buf, context) -> {
            PacketBrowserState pkt = new PacketBrowserState(buf);
            context.queue(() -> PacketBrowserState.handle(pkt, context));
        });
    }

    public static void sendToServer(BlockPos pos, String url, double volume) {
        PacketSetBrowserUrl pkt = new PacketSetBrowserUrl(pos, url, volume);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        pkt.encode(buf);

        NetworkManager.sendToServer(SET_BROWSER_URL_PACKET_ID, buf);
    }

    public static void requestState(BlockPos pos) {
        PacketRequestBrowserUrl pkt = new PacketRequestBrowserUrl(pos);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        pkt.encode(buf);

        NetworkManager.sendToServer(REQUEST_BROWSER_URL_PACKET_ID, buf);
    }

    public static void sendState(ServerPlayer player, BlockPos pos, String url, double volume) {
        PacketBrowserState pkt = new PacketBrowserState(pos, url, volume);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        pkt.encode(buf);

        NetworkManager.sendToPlayer(player, BROWSER_STATE_PACKET_ID, buf);
    }
}
