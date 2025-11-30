package top.yunmouren.craftbrowser.client.browser.api;

import top.yunmouren.craftbrowser.client.browser.core.BrowserManager;
import top.yunmouren.craftbrowser.client.browser.core.BrowserRender;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static top.yunmouren.craftbrowser.client.browser.util.JSScript.getCreateScript;
import static top.yunmouren.craftbrowser.client.browser.util.JSScript.getRemoveScript;

public class BrowserAPI {

    private static BrowserAPI INSTANCE;
    private final BrowserManager manager = new BrowserManager();
    private final BrowserRender render = new BrowserRender();
    private static final HashMap<String, BrowserSubprocess> Subprocess = new HashMap<>();

    /**
     * Main
     */
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
        BrowserAPI.getInstance().getManager().getBrowserFactory().runtime().evaluate(getCreateScript(
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
                Subprocess.put(OnlyKey, subprocess);
                callback.accept(subprocess);
            }
        });
    }

    public static BrowserSubprocess getBrowser(String OnlyKey) {
        return Subprocess.get(OnlyKey);
    }

    public BrowserRender getRender() {
        return render;
    }

    public BrowserManager getManager() {
        return manager;
    }

    // 新增：移除浏览器的辅助方法
    public static void removeBrowser(String OnlyKey) {
        if (Subprocess.containsKey(OnlyKey)) {
            BrowserSubprocess proc = Subprocess.remove(OnlyKey);
            if (proc != null) {
                proc.releaseSpout();
                proc.getBrowserFactory().close();
                BrowserAPI.getInstance().getManager().getBrowserFactory().runtime().evaluate(getRemoveScript(OnlyKey));
            }
        }
    }
}