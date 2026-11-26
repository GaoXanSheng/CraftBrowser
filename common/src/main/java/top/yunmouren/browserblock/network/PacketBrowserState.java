package top.yunmouren.browserblock.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import top.yunmouren.browserblock.client.BrowserClientHooks;

public class PacketBrowserState {
    private final BlockPos pos;
    private final String url;
    private final double volume;
    private final int pixelsPerBlock;
    private final int editPermission;

    public PacketBrowserState(BlockPos pos, String url, double volume, int pixelsPerBlock, int editPermission) {
        this.pos = pos;
        this.url = url;
        this.volume = volume;
        this.pixelsPerBlock = pixelsPerBlock;
        this.editPermission = editPermission;
    }

    public PacketBrowserState(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.url = buffer.readUtf(32767);
        this.volume = buffer.readDouble();
        this.pixelsPerBlock = buffer.readInt();
        this.editPermission = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeUtf(url);
        buffer.writeDouble(volume);
        buffer.writeInt(pixelsPerBlock);
        buffer.writeInt(editPermission);
    }

    public static void handle(PacketBrowserState pkt, NetworkManager.PacketContext context) {
        BrowserClientHooks.openBrowserScreen(pkt.pos, pkt.url, pkt.volume, pkt.pixelsPerBlock, pkt.editPermission);
    }
}
