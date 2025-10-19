package top.yunmouren.craftcdp;

import com.google.gson.JsonObject;

public record Input(Session session) {

    // --- Mouse Methods ---

    public void dispatchMouseEvent(String type, int x, int y, int modifiers,
                                   String button, int clickCount, int buttons,
                                   int deltaX, int deltaY) {
        if (session == null) return;

        JsonObject params = new JsonObject();
        params.addProperty("type", type);
        params.addProperty("x", x);
        params.addProperty("y", y);
        params.addProperty("modifiers", modifiers);
        params.addProperty("button", button);
        params.addProperty("clickCount", clickCount);
        if (type.equals("mouseWheel")) {
            params.addProperty("deltaX", deltaX);
            params.addProperty("deltaY", deltaY);
        }
        params.addProperty("buttons", buttons);
        session.send("Input.dispatchMouseEvent", params);
    }

    // --- Key Methods ---

    public void dispatchKeyEvent(String type, int modifiers, int windowsVirtualKeyCode, String code, String key, boolean isKeypad, int location, boolean isRepeat, String text, String unmodifiedText) {
        if (session == null) return;

        JsonObject params = new JsonObject();
        params.addProperty("type", type);
        params.addProperty("modifiers", modifiers);
        params.addProperty("windowsVirtualKeyCode", windowsVirtualKeyCode);
        params.addProperty("nativeVirtualKeyCode", windowsVirtualKeyCode); // 通常与 windowsVirtualKeyCode 相同
        params.addProperty("code", code);
        params.addProperty("key", key);
        params.addProperty("isKeypad", isKeypad);
        params.addProperty("location", location);
        params.addProperty("autoRepeat", isRepeat);

        if (text != null) {
            params.addProperty("text", text);
        }
        if (unmodifiedText != null) {
            params.addProperty("unmodifiedText", unmodifiedText);
        }

        session.send("Input.dispatchKeyEvent", params);
    }
}