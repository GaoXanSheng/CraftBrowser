package top.yunmouren.craftbrowser.client.browser.handler;

import top.yunmouren.craftbrowser.client.browser.cdp.BrowserFactory;

public record BrowserMouseHandler(BrowserFactory browserFactory) {

    public void mouseMove(int x, int y, boolean dragging) {
        if (browserFactory == null) return;
        String button = dragging ? "left" : "none";
        int buttons = dragging ? 1 : 0;
        browserFactory.input().dispatchMouseEvent("mouseMoved", x, y, 0, button, 0, buttons, 0, 0);
    }

    public void mousePress(int x, int y, int button) {
        if (browserFactory == null) return;

        String buttonStr = mapButton(button);
        int buttonsMask = mapButtonMask(button);

        browserFactory.input().dispatchMouseEvent("mousePressed", x, y, 0, buttonStr, 1, buttonsMask, 0, 0);
    }

    public void mouseRelease(int x, int y, int button) {
        if (browserFactory == null) return;

        String buttonStr = mapButton(button);
        int buttonsMask = mapButtonMask(button);

        browserFactory.input().dispatchMouseEvent("mouseReleased", x, y, 0, buttonStr, 1, buttonsMask, 0, 0);
    }

    public void mouseWheel(int x, int y, int deltaY) {
        if (browserFactory == null) return;
        browserFactory.input().dispatchMouseEvent("mouseWheel", x, y, 0, "none", 0, 0, 0, deltaY);
    }

    private String mapButton(int button) {
        return switch (button) {
            case 1 -> "right";
            case 2 -> "middle";
            default -> "left";
        };
    }

    private int mapButtonMask(int button) {
        return switch (button) {
            case 1 -> 2; // right
            case 2 -> 4; // middle
            default -> 1; // left
        };
    }
}
