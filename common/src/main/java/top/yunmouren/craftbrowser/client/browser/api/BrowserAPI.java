package top.yunmouren.craftbrowser.client.browser.api;

import net.minecraft.client.Minecraft;
import top.yunmouren.craftbrowser.client.browser.core.BrowserManager;
import top.yunmouren.craftbrowser.client.browser.core.BrowserRender;
import top.yunmouren.craftbrowser.client.browser.util.JSScript;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


public class BrowserAPI {

    private static BrowserAPI INSTANCE;
    private static final BrowserManager globalManager = new BrowserManager();
    private static final BrowserRender globalRender = new BrowserRender();
    private static final ConcurrentHashMap<String, BrowserSubprocess> Subprocess = new ConcurrentHashMap<>();

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

    public static void createBrowserAsync(String key, String url, int width, int height, int maxFps, Consumer<BrowserSubprocess> callback) {
        BrowserSubprocess existing = Subprocess.get(key);
        if (existing != null) {
            callback.accept(existing);
            return;
        }
        globalManager.getBrowserFactory()
                .runtime()
                .evaluate(JSScript.CreateBrowser(url, width, height, key, maxFps))
                .thenRunAsync(() -> {
                    try {
                        BrowserSubprocess again = Subprocess.get(key);
                        if (again != null) {
                            Minecraft.getInstance().execute(() -> callback.accept(again));
                            return;
                        }

                        BrowserSubprocess proc = new BrowserSubprocess(key);
                        Minecraft.getInstance().execute(() -> {
                            Subprocess.put(key, proc);
                            callback.accept(proc);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
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
                        globalManager.getBrowserFactory().runtime().evaluate(JSScript.CloseBrowser(OnlyKey));
                        proc.getBrowserFactory().close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}