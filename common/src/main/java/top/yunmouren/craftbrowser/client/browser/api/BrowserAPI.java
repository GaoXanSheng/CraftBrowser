package top.yunmouren.craftbrowser.client.browser.api;

import top.yunmouren.craftbrowser.client.browser.Controller.IBrowserController;
import top.yunmouren.craftbrowser.client.browser.Controller.IMasterController;
import top.yunmouren.craftbrowser.client.browser.Core.BrowserInstance;
import top.yunmouren.craftbrowser.client.browser.Core.BrowserRender;
import top.yunmouren.craftbrowser.client.browser.Rpc.BrowserEventBus;
import top.yunmouren.craftbrowser.client.browser.Rpc.RpcClient;
import top.yunmouren.craftbrowser.client.config.Config;
import top.yunmouren.httpserver.NcefUrls;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;


public class BrowserAPI {
    private static final long CONNECT_TIMEOUT_MS = 5000;
    private static final long CONNECT_RETRY_MS = 250;

    private static volatile IMasterController master;
    private static BrowserAPI INSTANCE;

    public static BrowserAPI getInstance() {
        if (INSTANCE == null) {
            synchronized (BrowserAPI.class) {
                if (INSTANCE == null) {
                    BrowserAPI.INSTANCE = new BrowserAPI();
                }
            }
        }
        return INSTANCE;
    }

    private static IMasterController master() {
        IMasterController current = master;
        if (current != null && BrowserInstance.isProcessAlive()) return current;

        synchronized (BrowserAPI.class) {
            if (master != null && BrowserInstance.isProcessAlive()) return master;
            master = null;

            long deadline = System.currentTimeMillis() + CONNECT_TIMEOUT_MS;
            RuntimeException last = null;

            while (System.currentTimeMillis() < deadline) {
                try {
                    master = RpcClient.create(IMasterController.class, Config.CLIENT.customizeRpc_ID.get());
                    return master;
                } catch (RuntimeException e) {
                    last = e;
                    if (!BrowserInstance.isProcessAlive()) {
                        throw new IllegalStateException("NCEF process is not running", last);
                    }
                    try {
                        Thread.sleep(CONNECT_RETRY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("interrupted while connecting to NCEF", ie);
                    }
                }
            }
            throw new IllegalStateException("NCEF not reachable within " + CONNECT_TIMEOUT_MS + "ms", last);
        }
    }

    public IBrowserController createBrowser(String url, int width, int height, int maxFps) {
        return RpcClient.create(IBrowserController.class, master().CreateBrowser(NcefUrls.resolve(url), width, height, maxFps));
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
        if (OnlyKey == null) return;
        try {
            master().StopBrowser(OnlyKey.GetSpoutId());
        } catch (Throwable ignored) {}

        if (Proxy.isProxyClass(OnlyKey.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(OnlyKey);
            if (handler instanceof RpcClient) {
                try {
                    ((RpcClient) handler).close();
                } catch (Throwable ignored) {}
            }
        }
    }
}
