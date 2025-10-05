package top.yunmouren.craftbrowser.client.browser.core;

import io.webfolder.cdp.session.Session;
import org.lwjgl.glfw.GLFW;
import top.yunmouren.craftbrowser.client.browser.util.CdpUtil;
import top.yunmouren.craftbrowser.client.browser.util.KeyEventMapper;
import top.yunmouren.craftbrowser.client.browser.util.KeyEventMapper.KeyEventInfo;

import java.util.HashMap;
import java.util.Map;

public record BrowserKeyHandler(Session session) {

    public void keyPress(int glfwKeyCode, int glfwModifiers, boolean isRelease, boolean isRepeat) {
        if (session == null) return;

        KeyEventInfo info = KeyEventMapper.mapGlfwToKeyEventInfo(glfwKeyCode);
        if (info == null) {
            System.err.println("Unhandled GLFW key code: " + glfwKeyCode);
            return;
        }

        boolean shiftDown = (glfwModifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        boolean capsLockOn = (glfwModifiers & GLFW.GLFW_MOD_CAPS_LOCK) != 0;

        String key = shiftDown ? info.shiftKey() : info.key();

        if (info.key().length() == 1 && Character.isLetter(info.key().charAt(0))) {
            if (capsLockOn ^ shiftDown) {
                key = info.shiftKey();
            } else {
                key = info.key();
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("modifiers", CdpUtil.mapGlfwModifiersToCdp(glfwModifiers));
        params.put("windowsVirtualKeyCode", info.windowsVirtualKeyCode());
        params.put("nativeVirtualKeyCode", info.windowsVirtualKeyCode());
        params.put("code", info.code());
        params.put("key", key);
        params.put("isKeypad", info.location() == 3);
        params.put("location", info.location());

        if (!isRelease) {
            params.put("type", "keyDown");
            params.put("autoRepeat", isRepeat);
            if (isPrintableKey(key)) {
                params.put("text", key);
                params.put("unmodifiedText", info.key());
            }
        } else {
            params.put("type", "keyUp");
            params.put("autoRepeat", false);
        }

        session.send("Input.dispatchKeyEvent", params);
    }

    public void keyChar(String text, int glfwModifiers) {
        if (session == null || text == null || text.isEmpty()) return;

        Map<String, Object> params = new HashMap<>();
        params.put("type", "char");
        params.put("text", text);
        params.put("unmodifiedText", text.toLowerCase());
        params.put("modifiers", CdpUtil.mapGlfwModifiersToCdp(glfwModifiers));

        session.send("Input.dispatchKeyEvent", params);
    }

    private boolean isPrintableKey(String key) {
        return key != null && key.length() == 1;
    }
}