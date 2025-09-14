package top.yunmouren.craftbrowser.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.yunmouren.craftbrowser.client.browser.MissingNCEFScreen;
import top.yunmouren.craftbrowser.client.browser.WebScreen;

import static top.yunmouren.craftbrowser.client.BrowserProcess.checkNCEFExists;

@Mixin(Minecraft.class)
public class CheckNCEFExistsLoadingScreen {
    @Unique
    private boolean craftBrowser$hasRedirected = false;

    @Inject(at = @At("HEAD"), method = "setScreen", cancellable = true)
    public void redirScreen(Screen guiScreen, CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;

        if (!craftBrowser$hasRedirected && !(guiScreen instanceof WebScreen)) {
            craftBrowser$hasRedirected = true;
            if (!checkNCEFExists()) {
                var missingNCEFScreen = new MissingNCEFScreen();
                mc.setScreen(missingNCEFScreen);
            }

            ci.cancel();
        }
    }
}
