package top.yunmouren.craftbrowser.client.browser.api;

import top.yunmouren.craftbrowser.client.browser.Controller.IBrowserController;
import top.yunmouren.craftbrowser.client.browser.Controller.IMasterController;
import top.yunmouren.craftbrowser.client.browser.Core.BrowserRender;
import top.yunmouren.craftbrowser.client.browser.Rpc.BrowserEventBus;
import top.yunmouren.craftbrowser.client.browser.Rpc.RpcClient;
import top.yunmouren.craftbrowser.client.config.Config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;


public class BrowserAPI {
    private static final IMasterController master = RpcClient.create(IMasterController.class, Config.CLIENT.customizeRpc_ID.get());

    private static BrowserAPI INSTANCE;

    public static BrowserAPI getInstance() {
        if (INSTANCE == null) {
            BrowserAPI.INSTANCE = new BrowserAPI();
        }
        return INSTANCE;
    }

    /**
     * Synchronization
     */

    public IBrowserController createBrowser(String url, int width, int height, int maxFps) {
        var hashed = master.CreateBrowser(url, width, height, maxFps);
        return RpcClient.create(IBrowserController.class, hashed);
    }

    public BrowserRender GetBrowserRender(IBrowserController browserController) {
        return new BrowserRender(browserController.GetSpoutId());
    }

    public BrowserEventBus GetBrowserEventBus(IBrowserController browserController) {
        if (browserController == null) {
            throw new IllegalArgumentException("BrowserController cannot be null");
        }
        if (Proxy.isProxyClass(browserController.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(browserController);
            if (handler instanceof RpcClient) {
                BrowserEventBus bus = ((RpcClient) handler).getEventBus();
                bus.setResponseChannel(browserController);
                return bus;
            }
        }
        throw new IllegalArgumentException("The passed browserController is not a valid RPC client proxy object！");
    }

    public void removeBrowser(IBrowserController OnlyKey) {
        master.StopBrowser(OnlyKey.GetSpoutId());
    }
}