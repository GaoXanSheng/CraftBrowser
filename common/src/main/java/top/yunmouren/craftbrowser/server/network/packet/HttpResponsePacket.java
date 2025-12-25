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
import java.util.concurrent.CompletableFuture;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;
@PacketConfig(side = NetworkManager.Side.S2C, async = false)
public class HttpResponsePacket extends AbstractJsonPacket<HttpResponsePacket> {

    public static final Type<HttpResponsePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "http_response"));

    private UUID requestId;
    private String responseData;

    public HttpResponsePacket() {}

    public HttpResponsePacket(UUID requestId, String responseData) {
        this.requestId = requestId;
        this.responseData = responseData;
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    protected void handle(NetworkManager.PacketContext context, Player player) {
        CompletableFuture<String> future = HttpNetworkHandler.getPendingFuture(this.requestId);
        if (future != null) {
            future.complete(this.responseData);
        }
    }
}