package top.yunmouren.craftbrowser.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import top.yunmouren.craftbrowser.fabric.server.network.FabricNetworkHandler;


public final class CraftbrowserFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricNetworkHandler.registerClientReceiver();
    }
}
