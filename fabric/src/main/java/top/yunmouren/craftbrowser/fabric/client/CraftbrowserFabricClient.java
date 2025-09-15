package top.yunmouren.craftbrowser.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import top.yunmouren.craftbrowser.fabric.client.config.FabricConfigHolder;
import top.yunmouren.craftbrowser.fabric.server.network.FabricNetworkHandler;

import java.io.File;

public final class CraftbrowserFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        File configDir = FabricLoader.getInstance().getConfigDir().toFile();
        FabricConfigHolder.register(configDir);
        FabricNetworkHandler.registerClientReceiver();
    }
}
