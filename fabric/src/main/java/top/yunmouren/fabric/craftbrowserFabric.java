package top.yunmouren.fabric;

import top.yunmouren.craftbrowser.Craftbrowser;
import net.fabricmc.api.ModInitializer;
import top.yunmouren.craftbrowser.server.network.BrowserNetworkHandler;

public final class craftbrowserFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        BrowserNetworkHandler.registerS2C();
        Craftbrowser.init();
    }
}
