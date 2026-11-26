package top.yunmouren.browserblock.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class BrowserClientHooks {

    public static void openBrowserScreen(BlockPos pos, String url, double volume, int pixelsPerBlock, int editPermission) {
        Minecraft.getInstance().setScreen(
                new BrowserUrlScreen(pos, url, volume, pixelsPerBlock, editPermission)
        );
    }
}
