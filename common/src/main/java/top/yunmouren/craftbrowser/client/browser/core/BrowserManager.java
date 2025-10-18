package top.yunmouren.craftbrowser.client.browser.core;

import com.hubspot.chrome.devtools.client.ChromeDevToolsSession;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import top.yunmouren.craftbrowser.client.config.Config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BrowserManager implements AutoCloseable {
    private final BrowserLifecycleManager lifecycleManager;
    private final BrowserMouseHandler mouseHandler;
    private final BrowserKeyHandler keyHandler;
    private final BrowserPageHandler pageHandler;

    public BrowserManager() {
        String host = "127.0.0.1";
        int port = Config.CLIENT.customizeBrowserPort.get();

        this.lifecycleManager = new BrowserLifecycleManager();
        this.lifecycleManager.initAsync(host, port);
        ChromeDevToolsSession session = this.lifecycleManager.getSession();

        this.mouseHandler = new BrowserMouseHandler(session);
        this.keyHandler = new BrowserKeyHandler(session);
        this.pageHandler = new BrowserPageHandler(session);
    }

    public BrowserManager(String host, int port) {
        this.lifecycleManager = new BrowserLifecycleManager();
        this.lifecycleManager.initAsync(host, port);
        ChromeDevToolsSession session = this.lifecycleManager.getSession();

        this.mouseHandler = new BrowserMouseHandler(session);
        this.keyHandler = new BrowserKeyHandler(session);
        this.pageHandler = new BrowserPageHandler(session);
    }

    public ChromeDevToolsSession getSession() {
        return lifecycleManager.getSession();
    }

    public void resizeViewport(int width, int height) {
        lifecycleManager.resizeViewport(width, height);
    }

    public void applyCursorFromChrome(int chromeCursorType) {
        long window = Minecraft.getInstance().getWindow().getWindow();
        long arrow = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
        long ibeam = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);
        long hand = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
        long cross = GLFW.glfwCreateStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR);

        long glfwCursor = arrow; // Default

        switch (chromeCursorType) {
            case 1: // IBEAM
                glfwCursor = ibeam;
                break;
            case 2: // Pointer/Hand
                glfwCursor = hand;
                break;
            case 3: // Crosshair
                glfwCursor = cross;
                break;
        }

        GLFW.glfwSetCursor(window, glfwCursor);
    }

    // --- Delegated Methods ---

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

    public void keyChar(String text, int glfwModifiers) {
        keyHandler.keyChar(text, glfwModifiers);
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