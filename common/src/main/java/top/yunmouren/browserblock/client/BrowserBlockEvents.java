package top.yunmouren.browserblock.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import top.yunmouren.craftbrowser.client.browser.Rpc.annotation.BrowserRpcEvent;

public class BrowserBlockEvents {

    @BrowserRpcEvent("scriptDialog")
    public String onScriptDialog(int kind, String message) {
        String label;
        switch (kind) {
            case 0 -> label = "alert";
            case 1 -> label = "confirm";
            case 2 -> label = "prompt";
            default -> label = "dialog";
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(
                    Component.literal("§b[Browser] §f" + label + ": " + message), false);
        }
        return "";
    }
}
