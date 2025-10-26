package top.yunmouren.craftbrowser.client.browser.core;

import top.yunmouren.craftbrowser.client.browser.util.CursorType;
import top.yunmouren.craftbrowser.client.config.Config;
import top.yunmouren.cdp.Browser;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class BrowserManager implements AutoCloseable {
    private final BrowserLifecycleManager lifecycleManager;
    private final BrowserMouseHandler mouseHandler;
    private final BrowserKeyHandler keyHandler;
    private final BrowserPageHandler pageHandler;
    private final AtomicReference<CursorType> currentCursor = new AtomicReference<>(CursorType.DEFAULT);

    public BrowserManager() {
        String host = "127.0.0.1";
        int port = Config.CLIENT.customizeBrowserPort.get();

        this.lifecycleManager = new BrowserLifecycleManager(host, port);
        var browser = this.lifecycleManager.getBrowser();

        this.mouseHandler = new BrowserMouseHandler(browser);
        this.keyHandler = new BrowserKeyHandler(browser);
        this.pageHandler = new BrowserPageHandler(browser);

        // 初始化光标监听
        initializeCursorListener(browser);
    }

    public BrowserManager(String host, int port) {
        this.lifecycleManager = new BrowserLifecycleManager(host, port);
        var browser = this.lifecycleManager.getBrowser();

        this.mouseHandler = new BrowserMouseHandler(browser);
        this.keyHandler = new BrowserKeyHandler(browser);
        this.pageHandler = new BrowserPageHandler(browser);

        // 初始化光标监听
        initializeCursorListener(browser);
    }

    /**
     * 初始化光标变化监听
     */
    private void initializeCursorListener(Browser browser) {
        if (browser == null) return;

        // 启用 Runtime 域以便执行 JavaScript
        browser.runtime().enable();
    }

    /**
     * 获取当前光标类型
     */
    public CursorType getCurrentCursor() {
        return currentCursor.get();
    }

    /**
     * 更新指定位置的光标样式
     * @param x 鼠标 X 坐标
     * @param y 鼠标 Y 坐标
     */
    public void updateCursorAtPosition(int x, int y) {
        Browser browser = lifecycleManager.getBrowser();
        if (browser == null) return;

        browser.runtime().getCursorAtPosition(x, y).thenAccept(cursorStyle -> {
            CursorType newCursor = CursorType.fromCssValue(cursorStyle);
            currentCursor.set(newCursor);
        });
    }

    public Browser getBrowser() {
        return lifecycleManager.getBrowser();
    }

    public void resizeViewport(int width, int height) {
        lifecycleManager.resizeViewport(width, height);
    }
    public void mouseMove(int x, int y, boolean dragging) {
        mouseHandler.mouseMove(x, y, dragging);
    }

    public void mousePress(int x, int y, int button) {
        mouseHandler.mousePress(x, y, button);
    }

    public void mouseRelease(int x, int y, int button) {
        mouseHandler.mouseRelease(x, y, button);
    }

    public void mouseWheel(int x, int y, int deltaY) {
        mouseHandler.mouseWheel(x, y, deltaY);
    }

    public void keyPress(int glfwKeyCode, int glfwModifiers, boolean isRelease, boolean isRepeat) {
        keyHandler.keyPress(glfwKeyCode, glfwModifiers, isRelease, isRepeat);
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingResizeTask = null;

    public void loadUrl(String url) {
        if (pendingResizeTask != null && !pendingResizeTask.isDone()) {
            pendingResizeTask.cancel(false);
        }
        int RESIZE_DELAY_MS = 200;
        pendingResizeTask = scheduler.schedule(() -> {
            pageHandler.loadUrl(url);
        }, RESIZE_DELAY_MS, TimeUnit.MILLISECONDS);

    }

    public void customizeLoadingScreenUrl() {
        pageHandler.customizeLoadingScreenUrl();
    }

    public void onClose() {
        lifecycleManager.onClose();
    }

    public void loadCustomizeURL(String url) {
        pageHandler.loadCustomizeURL(url);
    }

    @Override
    public void close() throws Exception {
        this.lifecycleManager.onClose();
    }
}