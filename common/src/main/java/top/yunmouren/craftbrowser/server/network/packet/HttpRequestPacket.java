package top.yunmouren.craftbrowser.server.network.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import top.yunmouren.craftbrowser.server.network.AbstractJsonPacket;
import top.yunmouren.craftbrowser.server.network.annotation.PacketConfig;
import top.yunmouren.httpserver.HttpNetworkHandler;

import java.util.UUID;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;
@PacketConfig(side = NetworkManager.Side.C2S, async = true)
public class HttpRequestPacket extends AbstractJsonPacket<HttpRequestPacket> {

    public static final Type<HttpRequestPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "http_request"));

    private UUID requestId;
    private String data;

    public HttpRequestPacket(UUID requestId, String data) {
        this.requestId = requestId;
        this.data = data;
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    protected void handle(NetworkManager.PacketContext context, Player player) {
        String response = HttpNetworkHandler.sendHttpToExternal(data);
        HttpResponsePacket reply = new HttpResponsePacket(requestId, response);
        NetworkManager.sendToPlayer((net.minecraft.server.level.ServerPlayer) player, reply);
    }
}