package top.yunmouren.browserblock.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import top.yunmouren.browserblock.block.BrowserBlockEntity;

public class PacketSetBrowserUrl {
    private final BlockPos pos;
    private final String url;

    // 普通构造函数
    public PacketSetBrowserUrl(BlockPos pos, String url) {
        this.pos = pos;
        this.url = url;
    }

    // 解码构造函数 (从 FriendlyByteBuf 读取)
    public PacketSetBrowserUrl(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.url = buffer.readUtf(32767);
    }

    // 编码方法
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeUtf(url);
    }

    // 处理逻辑
    public static void handle(PacketSetBrowserUrl pkt, NetworkManager.PacketContext context) {
        // 获取发送包的玩家
        Player player = context.getPlayer();

        // 确保是在服务端处理，且玩家对象有效
        if (player instanceof ServerPlayer) {
            Level level = player.level();

            // 检查区块是否加载，防止加载未加载的区块导致卡顿
            if (level.isLoaded(pkt.pos)) {
                BlockEntity be = level.getBlockEntity(pkt.pos);
                if (be instanceof BrowserBlockEntity browserEntity) {
                    browserEntity.setUrl(pkt.url);
                }
            }
        }
    }
}