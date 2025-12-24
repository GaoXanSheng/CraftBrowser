package top.yunmouren.craftbrowser.server.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.yunmouren.craftbrowser.server.command.CommandType;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public record BrowserPayload(
        CommandType command,
        String body
) implements CustomPacketPayload {

    public static final Type<BrowserPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "browser"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BrowserPayload> CODEC =
            StreamCodec.of(
                    BrowserPayload::encode,
                    BrowserPayload::decode
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(RegistryFriendlyByteBuf buf, BrowserPayload pkt) {
        buf.writeEnum(pkt.command);
        buf.writeUtf(pkt.body);
    }

    private static BrowserPayload decode(RegistryFriendlyByteBuf buf) {
        return new BrowserPayload(
                buf.readEnum(CommandType.class),
                buf.readUtf()
        );
    }
}