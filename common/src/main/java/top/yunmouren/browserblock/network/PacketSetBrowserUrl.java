package top.yunmouren.browserblock.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import top.yunmouren.browserblock.block.BrowserMasterBlockEntity;

public class PacketSetBrowserUrl {
    private final BlockPos pos;
    private final String url;
    private final double volume; // 新增音量字段

    public PacketSetBrowserUrl(BlockPos pos, String url, double volume) {
        this.pos = pos;
        this.url = url;
        this.volume = volume;
    }

    public PacketSetBrowserUrl(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.url = buffer.readUtf(32767);
        this.volume = buffer.readDouble();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeUtf(url);
        buffer.writeDouble(volume);
    }

    public static void handle(PacketSetBrowserUrl pkt, NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        if (player instanceof ServerPlayer) {
            Level level = player.level();
            if (level.isLoaded(pkt.pos)) {
                BlockEntity be = level.getBlockEntity(pkt.pos);
                if (be instanceof BrowserMasterBlockEntity browserEntity) {
                    browserEntity.setVolume(pkt.volume);
                    browserEntity.setUrl(pkt.url);
                }
            }
        }
    }
}