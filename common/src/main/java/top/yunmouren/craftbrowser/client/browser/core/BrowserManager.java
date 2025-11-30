package top.yunmouren.craftbrowser.client.browser.core;

import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.client.browser.handler.BrowserKeyHandler;
import top.yunmouren.craftbrowser.client.browser.handler.BrowserMouseHandler;
import top.yunmouren.craftbrowser.client.browser.handler.BrowserPageHandler;
import top.yunmouren.craftbrowser.client.browser.util.CursorType;
import top.yunmouren.craftbrowser.client.config.Config;
import top.yunmouren.craftbrowser.client.browser.cdp.BrowserFactory;

import java.util.concurrent.atomic.AtomicReference;

public class BrowserManager implements AutoCloseable {
    private BrowserFactory browserFactory;
    private final BrowserMouseHandler mouseHandler;
    private final BrowserKeyHandler keyHandler;
    private final BrowserPageHandler pageHandler;
    private final AtomicReference<CursorType> currentCursor = new AtomicReference<>(CursorType.DEFAULT);
    private static final String host = "127.0.0.1";
    private static final int port = Config.CLIENT.customizeBrowserPort.get();
    public BrowserManager() {
        this(host, port);
    }

    public BrowserManager(String host, int port) {
        try {
            this.browserFactory = BrowserFactory.launch(host, port);
        } catch (Exception e) {
            Craftbrowser.LOGGER.error("Failed to connect to ChromeDevTools", e);
        }
        this.mouseHandler = new BrowserMouseHandler(browserFactory);
        this.keyHandler = new BrowserKeyHandler(browserFactory);
        this.pageHandler = new BrowserPageHandler(browserFactory);
        initializeCursorListener(browserFactory);
    }

    public BrowserMouseHandler getMouseHandler() {
        return mouseHandler;
    }

    public BrowserKeyHandler getKeyHandler() {
        return keyHandler;
    }

    public BrowserPageHandler getPageHandler() {
        return pageHandler;
    }

    private void initializeCursorListener(BrowserFactory browserFactory) {
        if (browserFactory == null) return;
        browserFactory.runtime().enable();
    }

    public CursorType getCurrentCursor() {
        return currentCursor.get();
    }

    public void updateCursorAtPosition(int x, int y) {
        if (browserFactory == null) return;

        browserFactory.runtime().getCursorAtPosition(x, y).thenAccept(cursorStyle -> {
            CursorType newCursor = CursorType.fromCssValue(cursorStyle);
            currentCursor.set(newCursor);
        });
    }
    public BrowserFactory getBrowserFactory() {
        return browserFactory;
    }
    @Override
    public void close() {
        try {
            if (browserFactory != null) browserFactory.close();
        } catch (Exception e) {
            Craftbrowser.LOGGER.warn("Error closing browserFactory", e);
        }
    }
}