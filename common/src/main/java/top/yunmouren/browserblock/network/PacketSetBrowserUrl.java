package top.yunmouren.browserblock.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import top.yunmouren.browserblock.block.BrowserMasterBlockEntity;
import top.yunmouren.craftbrowser.client.config.Config;

public class PacketSetBrowserUrl {
    private final BlockPos pos;
    private final String url;
    private final double volume;
    private final int pixelsPerBlock;

    public PacketSetBrowserUrl(BlockPos pos, String url, double volume, int pixelsPerBlock) {
        this.pos = pos;
        this.url = url;
        this.volume = volume;
        this.pixelsPerBlock = pixelsPerBlock;
    }

    public PacketSetBrowserUrl(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.url = buffer.readUtf(32767);
        this.volume = buffer.readDouble();
        this.pixelsPerBlock = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeUtf(url);
        buffer.writeDouble(volume);
        buffer.writeInt(pixelsPerBlock);
    }

    public static void handle(PacketSetBrowserUrl pkt, NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        double range = Config.CLIENT.urlEditRangeBlocks.get();
        if (player.distanceToSqr(pkt.pos.getX() + 0.5, pkt.pos.getY() + 0.5, pkt.pos.getZ() + 0.5) > range * range) return;

        Level level = player.level();
        if (!level.isLoaded(pkt.pos)) return;

        BlockEntity be = level.getBlockEntity(pkt.pos);
        if (!(be instanceof BrowserMasterBlockEntity browserEntity)) return;

        if (!canEdit(serverPlayer, browserEntity)) {
            player.displayClientMessage(Component.literal("§cYou are not allowed to change this browser URL"), true);
            return;
        }

        browserEntity.setVolume(pkt.volume);
        browserEntity.setUrl(pkt.url);
        browserEntity.setPixelsPerBlock(pkt.pixelsPerBlock);
    }

    public static boolean canEdit(ServerPlayer player, BrowserMasterBlockEntity browser) {
        if (player.hasPermissions(2)) return true;
        if (Config.CLIENT.urlEditPermission.get() == 0) return true;
        if (browser.getOwner() == null) return true;
        return browser.getOwner().equals(player.getUUID());
    }
}
