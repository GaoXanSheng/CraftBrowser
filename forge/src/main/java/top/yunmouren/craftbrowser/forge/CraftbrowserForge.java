package top.yunmouren.craftbrowser.forge;

import net.minecraftforge.fml.DistExecutor;
import top.yunmouren.craftbrowser.Craftbrowser;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.yunmouren.craftbrowser.proxy.ClientProxy;
import top.yunmouren.craftbrowser.proxy.IProxy;
import top.yunmouren.craftbrowser.proxy.CommonProxy;

@Mod(Craftbrowser.MOD_ID)
public final class CraftbrowserForge {
    private static final IProxy proxy = DistExecutor.unsafeRunForDist(
            () -> ClientProxy::new,
            () -> CommonProxy::new
    );

    public CraftbrowserForge() {
        EventBuses.registerModEventBus(Craftbrowser.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Craftbrowser.init();
        proxy.init();
    }

}
