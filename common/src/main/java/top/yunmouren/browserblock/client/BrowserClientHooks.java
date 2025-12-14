package top.yunmouren.browserblock.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import top.yunmouren.browserblock.block.BrowserMasterBlockEntity;

@Environment(EnvType.CLIENT)
public class BrowserClientHooks {

    public static void openBrowserScreen(BrowserMasterBlockEntity be) {
        Minecraft.getInstance().setScreen(
                new BrowserUrlScreen(be)
        );
    }
}
