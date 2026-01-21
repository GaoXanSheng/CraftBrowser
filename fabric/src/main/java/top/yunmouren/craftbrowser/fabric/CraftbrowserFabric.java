package top.yunmouren.craftbrowser.fabric;

import top.yunmouren.browserblock.network.BrowserBlockNetworkHandler;
import top.yunmouren.craftbrowser.Craftbrowser;
import net.fabricmc.api.ModInitializer;
import top.yunmouren.craftbrowser.fabric.server.command.FabricCommand;
import top.yunmouren.craftbrowser.server.network.BrowserNetworkHandler;
import top.yunmouren.httpserver.HttpNetworkHandler;

public final class CraftbrowserFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricCommand.register();

        HttpNetworkHandler.registerC2SReceivers();
        top.yunmouren.browserblock.ModBlocks.register();
        BrowserBlockNetworkHandler.registerC2SReceivers();
        HttpNetworkHandler.registerS2CReceivers();
        BrowserNetworkHandler.getInstance().registerClientReceiver();
    }
}
