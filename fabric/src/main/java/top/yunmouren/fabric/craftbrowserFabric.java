package top.yunmouren.fabric;

import top.yunmouren.craftbrowser.Craftbrowser;
import net.fabricmc.api.ModInitializer;
import top.yunmouren.craftbrowser.server.network.NetworkRegistry;
import top.yunmouren.craftbrowser.server.network.packet.BrowserPacket;
import top.yunmouren.craftbrowser.server.network.packet.HttpResponsePacket;

public final class craftbrowserFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Craftbrowser.init();
        NetworkRegistry.register(BrowserPacket.class, BrowserPacket.TYPE);
        NetworkRegistry.register(HttpResponsePacket.class, HttpResponsePacket.TYPE);
        FabricCommand.register();
    }
}
