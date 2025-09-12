package top.yunmouren.craftbrowser.client.network;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.yunmouren.craftbrowser.client.browser.WebScreen;
import top.yunmouren.craftbrowser.server.network.NetworkMessageType;

@OnlyIn(Dist.CLIENT)
public class ClientEnumeration {
    public ClientEnumeration(NetworkMessageType messageType, String body) { // Changed to NetworkMessageType
        switch (messageType) { // Changed switch to use NetworkMessageType enum
            case OPEN_DEV: // Changed to enum constant
                Minecraft.getInstance().setScreen(new WebScreen());
                break;
            case LOAD_URL: // Changed to enum constant
                Minecraft.getInstance().setScreen(new WebScreen(body));
                break;
        }
    }
}
