package top.yunmouren.craftbrowser.client.browser;

import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.type.constant.MouseButtonType;
import io.webfolder.cdp.type.constant.MouseEventType;

import java.util.Map;

public class BrowserMouseHandler {
    private final Session session;

    public BrowserMouseHandler(Session session) {
        this.session = session;
    }

    public void mouseMove(int x, int y, boolean dragging) {
        if (session == null) return;
        MouseButtonType buttonType = dragging ? MouseButtonType.Left : MouseButtonType.None;
        Map<String, Object> params = Map.of(
                "type", MouseEventType.MouseMoved,
                "x", x,
                "y", y,
                "modifiers", 0,
                "button", buttonType,
                "clickCount", 0
        );
        session.send("Input.dispatchMouseEvent", params);
    }

    public void mousePress(int x, int y, int button) {
        if (session == null) return;
        Map<String, Object> params = Map.of(
                "type", MouseEventType.MousePressed, "x", x, "y", y,
                "modifiers", 0, "button", mapButton(button), "clickCount", 1
        );
        session.send("Input.dispatchMouseEvent", params);
    }

    public void mouseRelease(int x, int y, int button) {
        if (session == null) return;
        Map<String, Object> params = Map.of(
                "type", MouseEventType.MouseReleased, "x", x, "y", y,
                "modifiers", 0, "button", mapButton(button), "clickCount", 1
        );
        session.send("Input.dispatchMouseEvent", params);
    }

    public void mouseWheel(int x, int y, int deltaY) {
        if (session == null) return;
        Map<String, Object> params = Map.of(
                "type", MouseEventType.MouseWheel, "x", x, "y", y,
                "modifiers", 0, "button", "none", "clickCount", 0,
                "deltaX", 0, "deltaY", deltaY
        );
        session.send("Input.dispatchMouseEvent", params);
    }

    private MouseButtonType mapButton(int button) {
        return switch (button) {
            case 1 -> MouseButtonType.Right;
            case 2 -> MouseButtonType.Middle;
            default -> MouseButtonType.Left;
        };
    }
}