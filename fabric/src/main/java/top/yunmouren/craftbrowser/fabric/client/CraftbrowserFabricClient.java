package top.yunmouren.craftbrowser.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import top.yunmouren.craftbrowser.server.network.BrowserNetworkHandler;


public final class CraftbrowserFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BrowserNetworkHandler.getInstance().registerClientReceiver();
    }
}
