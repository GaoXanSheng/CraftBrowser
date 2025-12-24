package top.yunmouren.browserblock.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public class BrowserBlockNetworkHandler {
    public static final ResourceLocation SET_BROWSER_URL_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "set_browser_url");

    public record SetBrowserUrlPayload(
            BlockPos pos,
            String url,
            double volume
    ) implements CustomPacketPayload {

        public static final Type<SetBrowserUrlPayload> TYPE =
                new Type<>(SET_BROWSER_URL_PACKET_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, SetBrowserUrlPayload> CODEC =
                StreamCodec.of(
                        SetBrowserUrlPayload::encode,
                        SetBrowserUrlPayload::decode
                );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(RegistryFriendlyByteBuf buf, SetBrowserUrlPayload pkt) {
            buf.writeBlockPos(pkt.pos);
            buf.writeUtf(pkt.url);
            buf.writeDouble(pkt.volume);
        }

        private static SetBrowserUrlPayload decode(RegistryFriendlyByteBuf buf) {
            return new SetBrowserUrlPayload(
                    buf.readBlockPos(),
                    buf.readUtf(32767),
                    buf.readDouble()
            );
        }
    }


    public static void registerC2SReceivers() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                SetBrowserUrlPayload.TYPE,
                SetBrowserUrlPayload.CODEC,
                (payload, context) -> context.queue(() -> PacketSetBrowserUrl.handle(payload, context))
        );
    }

    public static void sendToServer(BlockPos pos, String url, double volume) {
        SetBrowserUrlPayload payload = new SetBrowserUrlPayload(pos, url, volume);
        NetworkManager.sendToServer(payload);
    }
}