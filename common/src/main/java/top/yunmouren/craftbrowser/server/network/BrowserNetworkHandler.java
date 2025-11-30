package top.yunmouren.craftbrowser.server.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import top.yunmouren.craftbrowser.client.network.ClientEnumeration;
import top.yunmouren.craftbrowser.server.command.CommandType;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public class BrowserNetworkHandler {

    private static final BrowserNetworkHandler INSTANCE = new BrowserNetworkHandler();

    public static BrowserNetworkHandler getInstance() {
        return INSTANCE;
    }

    public static final ResourceLocation BROWSER_PACKET_ID = new ResourceLocation(MOD_ID, "browser");

    public void registerClientReceiver() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, BROWSER_PACKET_ID, (buf, context) -> {
            BrowserPacket pkt = new BrowserPacket(buf);
            context.queue(() -> BrowserPacket.handle(pkt, context));
        });
    }

    public void sendOpenGui(ServerPlayer player) {
        sendToPlayer(player, CommandType.OPEN_GUI, "");
    }

    public void sendLoadUrl(ServerPlayer player, String url) {
        sendToPlayer(player, CommandType.LOAD_URL, url);
    }

    private void sendToPlayer(ServerPlayer player, CommandType type, String body) {
        BrowserPacket packet = new BrowserPacket(type, body);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);

        NetworkManager.sendToPlayer(player, BROWSER_PACKET_ID, buf);
    }

    public record BrowserPacket(CommandType type, String body) {

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(type.name());
            buf.writeUtf(body != null ? body : "");
        }

        public BrowserPacket(FriendlyByteBuf buf) {
            this(CommandType.valueOf(buf.readUtf()), buf.readUtf());
        }

        @SuppressWarnings("unused")
        public static void handle(BrowserPacket pkt, NetworkManager.PacketContext context) {
            new ClientEnumeration(pkt.type, pkt.body);
        }
    }
}

