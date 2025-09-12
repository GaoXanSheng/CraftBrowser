package top.yunmouren.craftbrowser.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.yunmouren.craftbrowser.client.browser.WebScreen;

@Mixin(Minecraft.class)
public class CustomLoading {
    @Unique
    private boolean craftBrowser$hasRedirected = false;

    @Inject(at = @At("HEAD"), method = "setScreen", cancellable = true)
    public void redirScreen(Screen guiScreen, CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;

        if (!craftBrowser$hasRedirected && !(guiScreen instanceof WebScreen)) {
            craftBrowser$hasRedirected = true;

            WebScreen webScreen = new WebScreen("http://127.0.0.1:5500/index.html", true);
            mc.setScreen(webScreen);

            ci.cancel();
        }
    }
}
