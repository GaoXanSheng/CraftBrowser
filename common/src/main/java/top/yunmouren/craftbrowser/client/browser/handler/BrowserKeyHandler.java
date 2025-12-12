package top.yunmouren.craftbrowser.client.browser.handler;

import org.lwjgl.glfw.GLFW;
import top.yunmouren.craftbrowser.client.browser.util.CdpUtil;
import top.yunmouren.craftbrowser.client.browser.util.KeyEventMapper;
import top.yunmouren.craftbrowser.client.browser.util.KeyEventMapper.KeyEventInfo;
import top.yunmouren.craftbrowser.client.browser.cdp.BrowserFactory;

public record BrowserKeyHandler(BrowserFactory browserFactory) {

    public void keyPress(int glfwKeyCode, int glfwModifiers, boolean isRelease, boolean isRepeat) {
        if (browserFactory == null) return;

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

        browserFactory.input().dispatchKeyEvent(
                type,
                cdpModifiers,
                info.windowsVirtualKeyCode(),
                info.code(),
                key,
                info.location() == 3,
                info.location(),
                isRepeat,
                text,
                unmodifiedText
        );
    }

    private boolean isPrintableKey(String key) {
        return key != null && key.length() == 1;
    }
}