package top.yunmouren.craftbrowser.client.browser.core;

import org.lwjgl.glfw.GLFW;
import top.yunmouren.craftbrowser.client.browser.util.CdpUtil;
import top.yunmouren.craftbrowser.client.browser.util.KeyEventMapper;
import top.yunmouren.craftbrowser.client.browser.util.KeyEventMapper.KeyEventInfo;
import top.yunmouren.cdp.Browser;

public record BrowserKeyHandler(Browser session) {

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
        String type = isRelease ? "keyUp" : "keyDown";
        int cdpModifiers = CdpUtil.mapGlfwModifiersToCdp(glfwModifiers);

        String text = null;
        String unmodifiedText = null;

        if (!isRelease && isPrintableKey(key)) {
            text = key;
            unmodifiedText = info.key();
        }

        // 调用我们手搓的库中 Input 模块的 dispatchKeyEvent 方法
        // 参数一一对应，代码更清晰
        session.input().dispatchKeyEvent(
                type,
                cdpModifiers,
                info.windowsVirtualKeyCode(),
                info.code(),
                key,
                info.location() == 3, // isKeypad
                info.location(),
                // 对于 keyUp，CDP 会忽略 autoRepeat，所以直接传递 isRepeat 是安全的
                isRepeat,
                text,
                unmodifiedText
        );
    }

    public void keyChar(String text, int glfwModifiers) {
        if (session == null || text == null || text.isEmpty()) return;
        int cdpModifiers = CdpUtil.mapGlfwModifiersToCdp(glfwModifiers);
        session.input().dispatchKeyEvent(
                "char",
                cdpModifiers,
                0,                      // windowsVirtualKeyCode not applicable
                null,                   // code not applicable
                null,                   // key not applicable
                false,                  // isKeypad not applicable
                0,                      // location not applicable
                false,                  // autoRepeat not applicable
                text,
                text.toLowerCase()      // unmodifiedText
        );

    }

    private boolean isPrintableKey(String key) {
        return key != null && key.length() == 1;
    }
}