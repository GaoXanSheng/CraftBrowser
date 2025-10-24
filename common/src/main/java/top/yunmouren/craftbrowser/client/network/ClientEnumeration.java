package top.yunmouren.craftbrowser.client.network;

import net.minecraft.client.Minecraft;
import top.yunmouren.craftbrowser.client.gui.WebScreen;

public class ClientEnumeration {
    public ClientEnumeration(String messageType, String body) {
        switch (messageType) {
            case "OPEN_DEV":
                Minecraft.getInstance().setScreen(new WebScreen());
                break;
            case "LOAD_URL":
                Minecraft.getInstance().setScreen(new WebScreen(body));
                break;
        }
    }
}
