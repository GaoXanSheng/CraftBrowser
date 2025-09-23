package top.yunmouren.craftbrowser.client.browser.util;

import org.lwjgl.glfw.GLFW;

public final class CdpUtil {

    private CdpUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Converts the GLFW modifier bitmask to the CDP modifier bitmask.
     */
    public static int mapGlfwModifiersToCdp(int glfwModifiers) {
        int cdpModifiers = 0;
        if ((glfwModifiers & GLFW.GLFW_MOD_ALT) != 0) cdpModifiers |= 1;
        if ((glfwModifiers & GLFW.GLFW_MOD_CONTROL) != 0) cdpModifiers |= 2;
        if ((glfwModifiers & GLFW.GLFW_MOD_SUPER) != 0) cdpModifiers |= 4; // Meta/Win key
        if ((glfwModifiers & GLFW.GLFW_MOD_SHIFT) != 0) cdpModifiers |= 8;
        if ((glfwModifiers & GLFW.GLFW_MOD_CAPS_LOCK) != 0) cdpModifiers |= 16;
        if ((glfwModifiers & GLFW.GLFW_MOD_NUM_LOCK) != 0) cdpModifiers |= 32;
        return cdpModifiers;
    }
}