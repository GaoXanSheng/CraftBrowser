package top.yunmouren.craftbrowser.server.network;

import com.google.gson.Gson;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.server.network.annotation.PacketConfig;


public abstract class AbstractJsonPacket<T extends AbstractJsonPacket<T>> implements CustomPacketPayload {

    private static final Gson GSON = new Gson();

    protected abstract void handle(NetworkManager.PacketContext context, Player player);

    public static <P extends AbstractJsonPacket<P>> StreamCodec<RegistryFriendlyByteBuf, P> createCodec(Class<P> type) {
        return StreamCodec.of(
                (buf, packet) -> {
                    String json = GSON.toJson(packet);
                    buf.writeUtf(json);
                },
                (buf) -> {
                    String json = buf.readUtf(32767);
                    return GSON.fromJson(json, type);
                }
        );
    }

    public static <P extends AbstractJsonPacket<P>> void handleInternal(P packet, NetworkManager.PacketContext context) {
        PacketConfig config = packet.getClass().getAnnotation(PacketConfig.class);
        Player player = context.getPlayer();
        Runnable work = () -> {
            try {
                packet.handle(context, player);
            } catch (Exception e) {
                Craftbrowser.LOGGER.error(e.getMessage());
            }
        };

        if (config != null && config.async()) {
            work.run();
        } else {
            context.queue(work);
        }
    }
}