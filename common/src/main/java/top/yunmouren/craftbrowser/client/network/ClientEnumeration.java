package top.yunmouren.craftbrowser.client.network;

import net.minecraft.client.Minecraft;
import top.yunmouren.craftbrowser.client.gui.WebScreen;
import top.yunmouren.craftbrowser.server.network.NetworkMessageType;

public class ClientEnumeration {
    public ClientEnumeration(NetworkMessageType messageType, String body) {
        switch (messageType) {
            case OPEN_DEV:
                Minecraft.getInstance().setScreen(new WebScreen());
                break;
            case LOAD_URL:
                Minecraft.getInstance().setScreen(new WebScreen(body));
                break;
        }
    }
}
