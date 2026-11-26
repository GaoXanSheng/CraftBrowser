package top.yunmouren.browserblock.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import top.yunmouren.browserblock.client.BrowserClientHooks;

public class PacketBrowserState {
    private final BlockPos pos;
    private final String url;
    private final double volume;

    public PacketBrowserState(BlockPos pos, String url, double volume) {
        this.pos = pos;
        this.url = url;
        this.volume = volume;
    }

    public PacketBrowserState(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.url = buffer.readUtf(32767);
        this.volume = buffer.readDouble();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeUtf(url);
        buffer.writeDouble(volume);
    }

    public static void handle(PacketBrowserState pkt, NetworkManager.PacketContext context) {
        BrowserClientHooks.openBrowserScreen(pkt.pos, pkt.url, pkt.volume);
    }
}
