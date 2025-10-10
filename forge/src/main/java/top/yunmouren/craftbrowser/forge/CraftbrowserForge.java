package top.yunmouren.craftbrowser.forge;

import de.keksuccino.fancymenu.customization.background.MenuBackgroundRegistry;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.yunmouren.craftbrowser.Craftbrowser;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.yunmouren.craftbrowser.fancymenu.BrowserMenuBackgroundBuilder;
import top.yunmouren.craftbrowser.proxy.ClientProxy;
import top.yunmouren.craftbrowser.proxy.IProxy;
import top.yunmouren.craftbrowser.proxy.ServerProxy;

@Mod(Craftbrowser.MOD_ID)
public final class CraftbrowserForge {
    private static final IProxy proxy = DistExecutor.unsafeRunForDist(
            () -> ClientProxy::new,
            () -> ServerProxy::new
    );

    public CraftbrowserForge() {
        EventBuses.registerModEventBus(Craftbrowser.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Craftbrowser.init();
        proxy.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }
    private void onClientSetup(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("fancymenu")) {
            MenuBackgroundRegistry.register(new BrowserMenuBackgroundBuilder("NCEF"));
        } else {
            Craftbrowser.LOGGER.info("FancyMenu not loaded, skipping BrowserMenuBackground registration.");
        }
    }
}
