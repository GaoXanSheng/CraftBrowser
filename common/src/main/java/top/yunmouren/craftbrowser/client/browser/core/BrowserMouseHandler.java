package top.yunmouren.craftbrowser.client.browser.core;

import com.hubspot.chrome.devtools.base.ChromeRequest;
import com.hubspot.chrome.devtools.client.ChromeDevToolsSession;

public record BrowserMouseHandler(ChromeDevToolsSession session) {

    public void mouseMove(int x, int y, boolean dragging) {
        if (session == null) return;
        String button = dragging ? "left" : "none";

        ChromeRequest request = new ChromeRequest("Input.dispatchMouseEvent")
                .putParams("type", "mouseMoved")
                .putParams("x", x)
                .putParams("y", y)
                .putParams("modifiers", 0)
                .putParams("button", button)
                .putParams("clickCount", 0);

        session.send(request);
    }

    public void mousePress(int x, int y, int button) {
        if (session == null) return;

        // 将整数按钮映射成 CDP 可识别的字符串
        String buttonStr = mapButton(button);

        ChromeRequest request = new ChromeRequest("Input.dispatchMouseEvent")
                .putParams("type", "mousePressed")
                .putParams("x", x)
                .putParams("y", y)
                .putParams("modifiers", 0)
                .putParams("button", buttonStr)
                .putParams("clickCount", 1);

        session.send(request);
    }

    public void mouseRelease(int x, int y, int button) {
        if (session == null) return;

        String buttonStr = mapButton(button);

        ChromeRequest request = new ChromeRequest("Input.dispatchMouseEvent")
                .putParams("type", "mouseReleased")
                .putParams("x", x)
                .putParams("y", y)
                .putParams("modifiers", 0)
                .putParams("button", buttonStr)
                .putParams("clickCount", 1);

        session.send(request);
    }

    public void mouseWheel(int x, int y, int deltaY) {
        if (session == null) return;

        ChromeRequest request = new ChromeRequest("Input.dispatchMouseEvent")
                .putParams("type", "mouseWheel")
                .putParams("x", x)
                .putParams("y", y)
                .putParams("modifiers", 0)
                .putParams("button", "none")
                .putParams("clickCount", 0)
                .putParams("deltaX", 0)
                .putParams("deltaY", deltaY);

        session.send(request);
    }


    private String mapButton(int button) {
        return switch (button) {
            case 1 -> "right";
            case 2 -> "middle";
            default -> "left";
        };
    }
}