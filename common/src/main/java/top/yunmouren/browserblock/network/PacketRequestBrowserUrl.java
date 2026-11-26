package top.yunmouren.browserblock.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import top.yunmouren.browserblock.block.BrowserMasterBlockEntity;
import top.yunmouren.craftbrowser.client.config.Config;

public class PacketRequestBrowserUrl {
    private final BlockPos pos;

    public PacketRequestBrowserUrl(BlockPos pos) {
        this.pos = pos;
    }

    public PacketRequestBrowserUrl(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    public static void handle(PacketRequestBrowserUrl pkt, NetworkManager.PacketContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) return;

        double range = Config.CLIENT.urlEditRangeBlocks.get();
        if (player.distanceToSqr(pkt.pos.getX() + 0.5, pkt.pos.getY() + 0.5, pkt.pos.getZ() + 0.5) > range * range) return;

        Level level = player.level();
        if (!level.isLoaded(pkt.pos)) return;
        if (!(level.getBlockEntity(pkt.pos) instanceof BrowserMasterBlockEntity be)) return;

        if (!PacketSetBrowserUrl.canEdit(player, be)) {
            player.displayClientMessage(Component.literal("§cYou are not allowed to change this browser URL"), true);
            return;
        }

        BrowserBlockNetworkHandler.sendState(player, pkt.pos, be.getUrl(), be.getVolume());
    }
}
