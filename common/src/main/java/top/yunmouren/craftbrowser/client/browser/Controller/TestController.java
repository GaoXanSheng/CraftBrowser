package top.yunmouren.craftbrowser.client.browser.Controller;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import top.yunmouren.craftbrowser.client.browser.Rpc.annotation.BrowserRpcController;
import top.yunmouren.craftbrowser.client.browser.Rpc.annotation.BrowserRpcEvent;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.util.datafix.fixes.BlockEntitySignTextStrictJsonFix.GSON;

@BrowserRpcController
public class TestController {

    private final Minecraft mc = Minecraft.getInstance();

    @BrowserRpcEvent("sendChat")
    public void onSendChat(String message) {
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal("§b[Web] §f" + message));
        }
    }

    @BrowserRpcEvent("add")
    public int onAdd(int a, int b) {
        System.out.println("Java calculating: " + a + " + " + b);
        return a + b;
    }

    @BrowserRpcEvent("getPlayerInfo")
    public String onGetPlayerInfo() {
        Map<String, Object> info = new HashMap<>();

        if (mc.player != null) {
            Player p = mc.player;
            info.put("name", p.getName().getString());
            info.put("uuid", p.getUUID().toString());
            info.put("x", Math.round(p.getX() * 100.0) / 100.0);
            info.put("y", Math.round(p.getY() * 100.0) / 100.0);
            info.put("z", Math.round(p.getZ() * 100.0) / 100.0);
            info.put("health", p.getHealth());
            info.put("isCreative", p.isCreative());
            info.put("inWorld", true);
        } else {
            info.put("inWorld", false);
            info.put("error", "Player not in world");
        }

        info.put("fps", Minecraft.getInstance().getFps());
        info.put("javaTime", System.currentTimeMillis());

        return GSON.toJson(info);
    }

    @BrowserRpcEvent("tpUp")
    public void onTeleportUp() {
        if (mc.player != null) {
            mc.player.setPos(mc.player.getX(), mc.player.getY() + 5, mc.player.getZ());
            mc.player.sendSystemMessage(Component.literal("§e[Web] §fTakeOff！"));
        }
    }
}