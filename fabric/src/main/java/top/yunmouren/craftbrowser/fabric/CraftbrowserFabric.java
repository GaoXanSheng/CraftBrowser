package top.yunmouren.craftbrowser.fabric;

import top.yunmouren.craftbrowser.Craftbrowser;
import net.fabricmc.api.ModInitializer;
import top.yunmouren.craftbrowser.fabric.server.command.FabricCommand;

public final class CraftbrowserFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Craftbrowser.init();
        FabricCommand.register();
    }
}
