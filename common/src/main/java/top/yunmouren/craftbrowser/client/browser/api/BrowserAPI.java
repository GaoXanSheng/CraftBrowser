package top.yunmouren.craftbrowser.client.browser.api;

import top.yunmouren.craftbrowser.client.browser.core.BrowserRender;
import top.yunmouren.craftbrowser.client.browser.rpc.RpcClient;
import top.yunmouren.craftbrowser.client.config.Config;


public class BrowserAPI {
    private static final IMasterController master = RpcClient.create(IMasterController.class, Config.CLIENT.customizeRpc_ID.get());

    private static BrowserAPI INSTANCE;

    public static BrowserAPI getInstance() {
        if (INSTANCE == null) {
            BrowserAPI.INSTANCE = new BrowserAPI();
        }
        return INSTANCE;
    }

    public IBrowserController createBrowser(String url, int width, int height, int maxFps) {
        var hashed = master.CreateBrowser(url, width, height, maxFps);
        return RpcClient.create(IBrowserController.class, hashed);
    }

    public BrowserRender GetBrowserRender(IBrowserController browserController) {
        return new BrowserRender(browserController.GetSpoutId());
    }

    public void removeBrowser(IBrowserController OnlyKey) {
        master.StopBrowser(OnlyKey.GetSpoutId());
    }
}