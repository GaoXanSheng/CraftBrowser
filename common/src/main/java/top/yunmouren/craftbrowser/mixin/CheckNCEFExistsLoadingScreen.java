package top.yunmouren.craftbrowser.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.yunmouren.craftbrowser.client.gui.MissingNCEFScreen;

import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(Minecraft.class)
public class CheckNCEFExistsLoadingScreen {
    @Unique
    private boolean craftBrowser$hasRedirected = false;

    @Inject(at = @At("HEAD"), method = "setScreen", cancellable = true)
    public void redirScreen(Screen guiScreen, CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;

        if (!craftBrowser$hasRedirected && !(guiScreen instanceof MissingNCEFScreen)) {
            craftBrowser$hasRedirected = true;

            if (!craftbrowser$checkNCEFExists()) {
                mc.execute(() -> mc.setScreen(new MissingNCEFScreen()));
                ci.cancel();
            }
        }
    }
    @Unique
    private static boolean craftbrowser$checkNCEFExists() {
        Path modsDir = Minecraft.getInstance().gameDirectory.toPath().resolve("NCEF");
        Path exePath = modsDir.resolve("NCEF.exe");

        return Files.exists(modsDir) && Files.isDirectory(modsDir)
                && Files.exists(exePath) && Files.isRegularFile(exePath);
    }
}
