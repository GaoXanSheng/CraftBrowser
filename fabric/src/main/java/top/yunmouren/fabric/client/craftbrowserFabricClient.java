package top.yunmouren.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import top.yunmouren.browserblock.network.BrowserBlockNetworkHandler;
import top.yunmouren.httpserver.HttpNetworkHandler;

public final class craftbrowserFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HttpNetworkHandler.registerC2SReceivers();
        HttpNetworkHandler.registerS2CReceivers();
        BrowserBlockNetworkHandler.registerC2SReceivers();
    }
}
