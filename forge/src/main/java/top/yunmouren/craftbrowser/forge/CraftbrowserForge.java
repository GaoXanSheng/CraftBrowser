package top.yunmouren.craftbrowser.forge;

import de.keksuccino.fancymenu.customization.background.MenuBackgroundRegistry;
import net.minecraftforge.fml.DistExecutor;
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
        try {
            Class.forName("de.keksuccino.fancymenu.FancyMenu");
            // 类存在，执行注册
            MenuBackgroundRegistry.register(new BrowserMenuBackgroundBuilder("NCEF"));
        } catch (ClassNotFoundException e) {
            // 类不存在，不做处理
            // 可以打印日志方便调试
            System.out.println("FancyMenu NotLoadedSkippingRegistration。");
        }
    }
}
