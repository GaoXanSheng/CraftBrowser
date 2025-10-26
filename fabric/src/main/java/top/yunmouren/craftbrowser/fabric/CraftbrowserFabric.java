package top.yunmouren.craftbrowser.fabric;

import top.yunmouren.craftbrowser.Craftbrowser;
import net.fabricmc.api.ModInitializer;
import top.yunmouren.craftbrowser.fabric.server.command.FabricCommand;

public final class CraftbrowserFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Craftbrowser.init();
        FabricCommand.register();
    }
}
