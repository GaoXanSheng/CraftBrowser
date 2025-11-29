package top.yunmouren.browserblock.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import top.yunmouren.browserblock.block.BrowserBlockEntity;

import java.util.function.Supplier;

public class PacketSetBrowserUrl {
    private final BlockPos pos;
    private final String url;

    public PacketSetBrowserUrl(BlockPos pos, String url) {
        this.pos = pos;
        this.url = url;
    }

    public PacketSetBrowserUrl(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.url = buffer.readUtf(32767); // 读取字符串
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeUtf(url);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                Level level = player.level();
                if (level.isLoaded(pos)) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof BrowserBlockEntity browserEntity) {
                        browserEntity.setUrl(url);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}