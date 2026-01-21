package top.yunmouren.craftbrowser.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.yunmouren.craftbrowser.Craftbrowser;

@Mixin(net.minecraft.client.main.Main.class)
public class MainMixin {
    @Inject(method = "main", at = @At("HEAD"),remap = false)
    private static void onGameStart(String[] args, CallbackInfo ci) {
        Craftbrowser.init();
    }
}