package top.yunmouren.craftbrowser.fabric.server.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import top.yunmouren.craftbrowser.client.network.ClientEnumeration;
import io.netty.buffer.Unpooled;
import top.yunmouren.craftbrowser.server.network.NetworkMessageType;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public class FabricNetworkHandler {

    private static final ResourceLocation CHANNEL_ID = new ResourceLocation(MOD_ID, "main");

    /** 向指定玩家发送消息 */
    public static void sendToPlayer(ServerPlayer player, NetworkMessageType type, String body) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        encode(buf, type, body);
        ServerPlayNetworking.send(player, CHANNEL_ID, buf);
    }

    /** 编码消息 */
    private static void encode(FriendlyByteBuf buf, NetworkMessageType type, String body) {
        buf.writeUtf(type.name());  // 写入枚举名
        buf.writeUtf(body != null ? body : "");
    }

    /** 注册客户端消息处理（必须在客户端初始化时调用） */
    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL_ID, (client, handler, buf, responseSender) -> {
            String typeName = buf.readUtf();
            String body = buf.readUtf();
            NetworkMessageType type = NetworkMessageType.fromId(typeName);
            client.execute(() -> new ClientEnumeration(type, body));
        });
    }
}
