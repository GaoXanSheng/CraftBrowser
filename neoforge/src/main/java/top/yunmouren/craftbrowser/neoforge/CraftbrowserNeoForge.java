package top.yunmouren.craftbrowser.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.proxy.ClientProxy;
import top.yunmouren.craftbrowser.proxy.CommonProxy;

@Mod(Craftbrowser.MOD_ID)
public final class CraftbrowserNeoForge {

    public CraftbrowserNeoForge() {
        Craftbrowser.init();

        CommonProxy proxy =
                FMLEnvironment.dist == Dist.CLIENT
                        ? new ClientProxy()
                        : new CommonProxy();

        proxy.init();
    }
}
