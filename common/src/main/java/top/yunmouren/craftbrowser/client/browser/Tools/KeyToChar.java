package top.yunmouren.craftbrowser.client.browser.Tools;

import static org.lwjgl.glfw.GLFW.*;

public class KeyToChar {
    /**
     * Convert a GLFW key + modifiers into Windows Virtual-Key code.
     *
     * @param keyCode   GLFW key code (GLFW_KEY_*)
     * @param scanCode  Scan code (unused in this version, but could be used for special mapping)
     * @param modifiers GLFW modifiers (GLFW_MOD_SHIFT, GLFW_MOD_CONTROL, GLFW_MOD_ALT)
     * @return Windows Virtual-Key code
     */
    public static int getWindowsKeyCode(int keyCode, int scanCode, int modifiers) {
        boolean shift = (modifiers & GLFW_MOD_SHIFT) != 0;

        if (keyCode >= GLFW_KEY_A && keyCode <= GLFW_KEY_Z) {
            return 0x41 + (keyCode - GLFW_KEY_A); // 'A'..'Z'
        }

        if (keyCode >= GLFW_KEY_0 && keyCode <= GLFW_KEY_9) {
            return 0x30 + (keyCode - GLFW_KEY_0); // '0'..'9'
        }

        if (keyCode >= GLFW_KEY_F1 && keyCode <= GLFW_KEY_F12) {
            return 0x70 + (keyCode - GLFW_KEY_F1); // VK_F1..VK_F12
        }

        switch (keyCode) {
            case GLFW_KEY_LEFT: return 0x25;
            case GLFW_KEY_RIGHT: return 0x27;
            case GLFW_KEY_UP: return 0x26;
            case GLFW_KEY_DOWN: return 0x28;
        }

        switch (keyCode) {
            case GLFW_KEY_ESCAPE: return 0x1B;
            case GLFW_KEY_ENTER: return 0x0D;
            case GLFW_KEY_TAB: return 0x09;
            case GLFW_KEY_BACKSPACE: return 0x08;
            case GLFW_KEY_SPACE: return 0x20;
            case GLFW_KEY_INSERT: return 0x2D;
            case GLFW_KEY_DELETE: return 0x2E;
            case GLFW_KEY_HOME: return 0x24;
            case GLFW_KEY_END: return 0x23;
            case GLFW_KEY_PAGE_UP: return 0x21;
            case GLFW_KEY_PAGE_DOWN: return 0x22;
            case GLFW_KEY_PAUSE: return 0x13;
            case GLFW_KEY_CAPS_LOCK: return 0x14;
            case GLFW_KEY_NUM_LOCK: return 0x90;
            case GLFW_KEY_SCROLL_LOCK: return 0x91;
        }

        switch (keyCode) {
            case GLFW_KEY_LEFT_SHIFT:
            case GLFW_KEY_RIGHT_SHIFT: return 0x10;
            case GLFW_KEY_LEFT_CONTROL:
            case GLFW_KEY_RIGHT_CONTROL: return 0x11;
            case GLFW_KEY_LEFT_ALT:
            case GLFW_KEY_RIGHT_ALT: return 0x12;
        }
        if (shift) {
            switch (keyCode) {
                case GLFW_KEY_1: return 0x31;
                case GLFW_KEY_2: return 0x32;
                case GLFW_KEY_3: return 0x33;
                case GLFW_KEY_4: return 0x34;
                case GLFW_KEY_5: return 0x35;
                case GLFW_KEY_6: return 0x36;
                case GLFW_KEY_7: return 0x37;
                case GLFW_KEY_8: return 0x38;
                case GLFW_KEY_9: return 0x39;
                case GLFW_KEY_0: return 0x30;
            }
        }
        return keyCode;
    }
    public static char getCharFromKeyCode(int keyCode, int modifiers) {
        if (keyCode >= GLFW_KEY_A && keyCode <= GLFW_KEY_Z) {
            boolean shift = (modifiers & GLFW_MOD_SHIFT) != 0;
            return (char) ((shift ? 'A' : 'a') + (keyCode - GLFW_KEY_A));
        }
        if (keyCode >= GLFW_KEY_0 && keyCode <= GLFW_KEY_9) {
            return (char) ('0' + (keyCode - GLFW_KEY_0));
        }
        if (keyCode == GLFW_KEY_SPACE) return ' ';
        return 0;
    }

}
