package top.yunmouren.craftbrowser.client.browser.api;

import top.yunmouren.craftbrowser.client.browser.core.BrowserManager;
import top.yunmouren.craftbrowser.client.browser.core.BrowserRender;

import java.util.HashMap;

public class BrowserAPI {

    private static BrowserAPI INSTANCE;
    private final BrowserManager manager = new BrowserManager();
    private final BrowserRender render = new BrowserRender();
    private final HashMap<String, BrowserSubprocess> Subprocess = new HashMap<>();

    /**
     * MainThread
     */
    public static BrowserAPI getInstance() {
        if (INSTANCE == null) {
            BrowserAPI.INSTANCE = new BrowserAPI();
        }
        return INSTANCE;
    }

    public BrowserSubprocess createSubprocess(String OnlyKey, String Url, int width, int height, int MaxFps) {
        if (Subprocess.containsKey(OnlyKey)) {
            return Subprocess.get(OnlyKey);
        }
        BrowserSubprocess subprocess = new BrowserSubprocess(Url, width, height, OnlyKey, MaxFps);
        Subprocess.put(OnlyKey, subprocess);
        return subprocess;
    }

    public BrowserSubprocess getSubprocess(String OnlyKey) {
        return Subprocess.get(OnlyKey);
    }

    public BrowserRender getRender() {
        return render;
    }

    public BrowserManager getManager() {
        return manager;
    }
}
