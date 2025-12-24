package top.yunmouren.browserblock.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import top.yunmouren.browserblock.block.BrowserMasterBlockEntity;

public class PacketSetBrowserUrl {
    public static void handle(BrowserBlockNetworkHandler.SetBrowserUrlPayload pkt, NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        if (player instanceof ServerPlayer) {
            Level level = player.level();
            if (level.isLoaded(pkt.pos())) {
                BlockEntity be = level.getBlockEntity(pkt.pos());
                if (be instanceof BrowserMasterBlockEntity browserEntity) {
                    browserEntity.setVolume(pkt.volume());
                    browserEntity.setUrl(pkt.url());
                }
            }
        }
    }
}