package top.yunmouren.craftbrowser;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import top.yunmouren.craftbrowser.proxy.ClientProxy;
import top.yunmouren.craftbrowser.proxy.IProxy;
import top.yunmouren.craftbrowser.proxy.ServerProxy;

@Mod("craftbrowser")
public class Craftbrowser {
    public static final String MOD_ID = "craftbrowser";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final IProxy proxy = DistExecutor.safeRunForDist(
            () -> ClientProxy::new,
            () -> ServerProxy::new
    );

    public Craftbrowser() {
        proxy.init();
    }
}
