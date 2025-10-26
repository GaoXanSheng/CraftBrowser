package top.yunmouren.craftbrowser.client.network;

import net.minecraft.client.Minecraft;
import top.yunmouren.craftbrowser.client.gui.WebScreen;
import top.yunmouren.craftbrowser.server.command.CommandType;

public class ClientEnumeration {
    public ClientEnumeration(CommandType messageType, String body) {
        switch (messageType) {
            case OPEN_GUI:
                Minecraft.getInstance().setScreen(new WebScreen());
                break;
            case LOAD_URL:
                Minecraft.getInstance().setScreen(new WebScreen(body));
                break;
        }
    }
}
