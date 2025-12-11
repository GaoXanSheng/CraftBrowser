package top.yunmouren.craftbrowser.client.browser.api;

import net.minecraft.client.Minecraft;
import top.yunmouren.craftbrowser.client.browser.core.BrowserManager;
import top.yunmouren.craftbrowser.client.browser.core.BrowserRender;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static top.yunmouren.craftbrowser.client.browser.util.JSScript.getCreateScript;
import static top.yunmouren.craftbrowser.client.browser.util.JSScript.getRemoveScript;

public class BrowserAPI {

    private static BrowserAPI INSTANCE;
    private static final BrowserManager globalManager = new BrowserManager();
    private static final BrowserRender globalRender = new BrowserRender();
    private static final HashMap<String, BrowserSubprocess> Subprocess = new HashMap<>();

    public static BrowserManager getGlobalManager() {
        return globalManager;
    }

    public static BrowserRender getGlobalRender() {
        return globalRender;
    }

    public static BrowserAPI getInstance() {
        if (INSTANCE == null) {
            BrowserAPI.INSTANCE = new BrowserAPI();
        }
        return INSTANCE;
    }

    public static void createBrowserAsync(String OnlyKey, String Url, int width, int height, int MaxFps, Consumer<BrowserSubprocess> callback) {
        if (Subprocess.containsKey(OnlyKey)) {
            callback.accept(Subprocess.get(OnlyKey));
            return;
        }
        globalManager.getBrowserFactory().runtime().evaluate(getCreateScript(
                Url, width, height, OnlyKey, MaxFps
        ));
        CompletableFuture.supplyAsync(() -> {
            try {
                return new BrowserSubprocess(OnlyKey);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAcceptAsync(subprocess -> {
            if (subprocess != null) {
                Minecraft.getInstance().execute(() -> {
                    Subprocess.put(OnlyKey, subprocess);
                    callback.accept(subprocess);
                });
            }
        });
    }

    public static BrowserSubprocess getBrowser(String OnlyKey) {
        return Subprocess.get(OnlyKey);
    }

    public static void removeBrowser(String OnlyKey) {
        if (Subprocess.containsKey(OnlyKey)) {
            BrowserSubprocess proc = Subprocess.remove(OnlyKey);
            if (proc != null) {
                Minecraft.getInstance().execute(() -> {
                    try {
                        proc.releaseSpout();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                CompletableFuture.runAsync(() -> {
                    try {
                        globalManager.getBrowserFactory().runtime().evaluate(getRemoveScript(OnlyKey));
                        proc.getBrowserFactory().close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}