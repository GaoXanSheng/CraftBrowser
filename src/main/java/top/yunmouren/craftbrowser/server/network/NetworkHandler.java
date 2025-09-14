package top.yunmouren.craftbrowser.server.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkRegistry;
import top.yunmouren.craftbrowser.client.network.ClientEnumeration;

import java.util.Optional;
import java.util.function.Supplier;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1.0";
    private static int packetId = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,               // Supplier<String>
            PROTOCOL_VERSION::equals,      // Predicate<String> 客户端
            PROTOCOL_VERSION::equals       // Predicate<String> 服务端
    );

    public static void register() {
        registerMessage(SimpleNetwork.class,
                SimpleNetwork::encode,
                SimpleNetwork::decode,
                SimpleNetwork::handle,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }

    private static <MSG> void registerMessage(Class<MSG> clazz,
                                              IMessageEncoder<MSG> encoder,
                                              IMessageDecoder<MSG> decoder,
                                              IMessageHandler<MSG> handler,
                                              NetworkDirection direction) {
        CHANNEL.registerMessage(packetId++, clazz, encoder::encode, decoder::decode, handler::handle, Optional.of(direction));
    }

    public static void sendToPlayer(ServerPlayer player, NetworkMessageType type, String body) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SimpleNetwork(type, body));
    }

    // ------------------ 消息类 ------------------
    public record SimpleNetwork(NetworkMessageType head, String body) {

        public static void encode(SimpleNetwork msg, FriendlyByteBuf buffer) {
            buffer.writeEnum(msg.head);
            buffer.writeUtf(msg.body != null ? msg.body : "");
        }

        public static SimpleNetwork decode(FriendlyByteBuf buffer) {
            NetworkMessageType type = buffer.readEnum(NetworkMessageType.class);
            String body = buffer.readUtf();
            return new SimpleNetwork(type, body);
        }

        public static void handle(SimpleNetwork msg, Supplier<NetworkEvent.Context> ctxSup) {
            NetworkEvent.Context ctx = ctxSup.get();
            ctx.enqueueWork(() -> new ClientEnumeration(msg.head, msg.body));
            ctx.setPacketHandled(true);
        }
    }

    // ------------------ 泛型接口 ------------------
    @FunctionalInterface
    public interface IMessageEncoder<MSG> { void encode(MSG msg, FriendlyByteBuf buf); }

    @FunctionalInterface
    public interface IMessageDecoder<MSG> { MSG decode(FriendlyByteBuf buf); }

    @FunctionalInterface
    public interface IMessageHandler<MSG> { void handle(MSG msg, Supplier<NetworkEvent.Context> ctxSup); }
}
