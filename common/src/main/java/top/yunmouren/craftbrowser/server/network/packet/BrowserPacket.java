package top.yunmouren.craftbrowser.server.network.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import top.yunmouren.craftbrowser.client.network.ClientEnumeration; // 假设这是你的逻辑类
import top.yunmouren.craftbrowser.server.command.CommandType;
import top.yunmouren.craftbrowser.server.network.AbstractJsonPacket;
import top.yunmouren.craftbrowser.server.network.annotation.PacketConfig;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

@PacketConfig(side = NetworkManager.Side.S2C)
public class BrowserPacket extends AbstractJsonPacket<BrowserPacket> {

    public static final Type<BrowserPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "browser"));

    private CommandType command;
    private String body;

    public BrowserPacket() {}

    public BrowserPacket(CommandType command, String body) {
        this.command = command;
        this.body = body == null ? "" : body;
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    protected void handle(NetworkManager.PacketContext context, Player player) {
        new ClientEnumeration(this.command, this.body);
    }
}