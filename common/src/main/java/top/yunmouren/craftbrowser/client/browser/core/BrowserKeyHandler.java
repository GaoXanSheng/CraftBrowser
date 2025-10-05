package top.yunmouren.craftbrowser.client.browser.core;

import com.hubspot.chrome.devtools.base.ChromeRequest;
import com.hubspot.chrome.devtools.client.ChromeDevToolsSession;
import org.lwjgl.glfw.GLFW;
import top.yunmouren.craftbrowser.client.browser.util.CdpUtil;
import top.yunmouren.craftbrowser.client.browser.util.KeyEventMapper;
import top.yunmouren.craftbrowser.client.browser.util.KeyEventMapper.KeyEventInfo;

import java.util.HashMap;
import java.util.Map;

public record BrowserKeyHandler(ChromeDevToolsSession session) {

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

        ChromeRequest request = new ChromeRequest("Input.dispatchKeyEvent");

        if (!isRelease) {
            request.putParams("type", "keyDown")
                    .putParams("autoRepeat", isRepeat);
            if (isPrintableKey(key)) {
                request.putParams("text", key)
                        .putParams("unmodifiedText", info.key());
            }
        } else {
            request.putParams("type", "keyUp")
                    .putParams("autoRepeat", false);
        }

        request.putParams("modifiers", CdpUtil.mapGlfwModifiersToCdp(glfwModifiers))
                .putParams("windowsVirtualKeyCode", info.windowsVirtualKeyCode())
                .putParams("nativeVirtualKeyCode", info.windowsVirtualKeyCode())
                .putParams("code", info.code())
                .putParams("key", key)
                .putParams("isKeypad", info.location() == 3)
                .putParams("location", info.location());

        session.send(request);

    }

    public void keyChar(String text, int glfwModifiers) {
        if (session == null || text == null || text.isEmpty()) return;

        ChromeRequest request = new ChromeRequest("Input.dispatchKeyEvent")
                .putParams("type", "char")
                .putParams("text", text)
                .putParams("unmodifiedText", text.toLowerCase())
                .putParams("modifiers", CdpUtil.mapGlfwModifiersToCdp(glfwModifiers));

        session.send(request);

    }

    private boolean isPrintableKey(String key) {
        return key != null && key.length() == 1;
    }
}