package top.yunmouren.browserblock.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel CHANNEL;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MOD_ID,"browserblock"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        int id = 0;
        CHANNEL.registerMessage(id++, PacketSetBrowserUrl.class,
                PacketSetBrowserUrl::toBytes,
                PacketSetBrowserUrl::new,
                PacketSetBrowserUrl::handle
        );
    }
}