package top.yunmouren.craftbrowser.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.proxy.ClientProxy;
import top.yunmouren.craftbrowser.proxy.CommonProxy;

@Mod(Craftbrowser.MOD_ID)
public final class CraftbrowserNeoForge {

    public CraftbrowserNeoForge(IEventBus eventBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientProxy.init(eventBus);
            Craftbrowser.init();
        }
        CommonProxy.init(eventBus);
    }
}
