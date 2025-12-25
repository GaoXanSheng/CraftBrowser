package top.yunmouren.craftbrowser.server.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import top.yunmouren.craftbrowser.server.network.annotation.PacketConfig;

public class NetworkRegistry {

    public static <T extends AbstractJsonPacket<T>>
    void register(Class<T> clazz, CustomPacketPayload.Type<T> type) {

        PacketConfig config =
                clazz.getAnnotation(PacketConfig.class);

        if (config == null) {
            throw new IllegalArgumentException(
                    "Missing @PacketConfig on " + clazz.getName()
            );
        }
        StreamCodec<RegistryFriendlyByteBuf, T> codec =
                AbstractJsonPacket.createCodec(clazz);
        NetworkManager.Side side = config.side();
        NetworkManager.registerReceiver(
                side,
                type,
                codec,
                AbstractJsonPacket::handleInternal
        );

    }
}
