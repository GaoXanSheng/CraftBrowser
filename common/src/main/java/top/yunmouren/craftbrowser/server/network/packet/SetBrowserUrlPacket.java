package top.yunmouren.craftbrowser.server.network.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import top.yunmouren.browserblock.block.BrowserMasterBlockEntity;
import top.yunmouren.craftbrowser.server.network.AbstractJsonPacket;
import top.yunmouren.craftbrowser.server.network.annotation.PacketConfig;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

@PacketConfig(side = NetworkManager.Side.C2S)
public class SetBrowserUrlPacket extends AbstractJsonPacket<SetBrowserUrlPacket> {

    public static final Type<SetBrowserUrlPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "set_browser_url"));

    private BlockPos pos;
    private String url;
    private double volume;

    public SetBrowserUrlPacket(BlockPos pos, String url, double volume) {
        this.pos = pos;
        this.url = url;
        this.volume = volume;
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    protected void handle(NetworkManager.PacketContext context, Player player) {
        Level level = player.level();
        if (level.isLoaded(pos)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BrowserMasterBlockEntity browserEntity) {
                browserEntity.setVolume(volume);
                browserEntity.setUrl(url);
            }
        }
    }
}