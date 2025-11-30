package top.yunmouren.craftbrowser.client.browser.util;

import org.lwjgl.glfw.GLFW;

public final class KeyEventMapper {

    public record KeyEventInfo(
            String key,                 // e.g., "a"
            String shiftKey,            // e.g., "A"
            String code,                // e.g., "KeyA"
            int windowsVirtualKeyCode,
            int location
    ) {
    }

    private KeyEventMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Maps a GLFW key code to a comprehensive KeyEventInfo object required by CDP.
     */
    public static KeyEventInfo mapGlfwToKeyEventInfo(int keyCode) {
        // DOM Key Location Constants: 0=Standard, 1=Left, 2=Right, 3=Numpad
        return switch (keyCode) {
            // --- Alphanumeric Keys ---
            case GLFW.GLFW_KEY_A -> new KeyEventInfo("a", "A", "KeyA", 0x41, 0);
            case GLFW.GLFW_KEY_B -> new KeyEventInfo("b", "B", "KeyB", 0x42, 0);
            case GLFW.GLFW_KEY_C -> new KeyEventInfo("c", "C", "KeyC", 0x43, 0);
            case GLFW.GLFW_KEY_D -> new KeyEventInfo("d", "D", "KeyD", 0x44, 0);
            case GLFW.GLFW_KEY_E -> new KeyEventInfo("e", "E", "KeyE", 0x45, 0);
            case GLFW.GLFW_KEY_F -> new KeyEventInfo("f", "F", "KeyF", 0x46, 0);
            case GLFW.GLFW_KEY_G -> new KeyEventInfo("g", "G", "KeyG", 0x47, 0);
            case GLFW.GLFW_KEY_H -> new KeyEventInfo("h", "H", "KeyH", 0x48, 0);
            case GLFW.GLFW_KEY_I -> new KeyEventInfo("i", "I", "KeyI", 0x49, 0);
            case GLFW.GLFW_KEY_J -> new KeyEventInfo("j", "J", "KeyJ", 0x4A, 0);
            case GLFW.GLFW_KEY_K -> new KeyEventInfo("k", "K", "KeyK", 0x4B, 0);
            case GLFW.GLFW_KEY_L -> new KeyEventInfo("l", "L", "KeyL", 0x4C, 0);
            case GLFW.GLFW_KEY_M -> new KeyEventInfo("m", "M", "KeyM", 0x4D, 0);
            case GLFW.GLFW_KEY_N -> new KeyEventInfo("n", "N", "KeyN", 0x4E, 0);
            case GLFW.GLFW_KEY_O -> new KeyEventInfo("o", "O", "KeyO", 0x4F, 0);
            case GLFW.GLFW_KEY_P -> new KeyEventInfo("p", "P", "KeyP", 0x50, 0);
            case GLFW.GLFW_KEY_Q -> new KeyEventInfo("q", "Q", "KeyQ", 0x51, 0);
            case GLFW.GLFW_KEY_R -> new KeyEventInfo("r", "R", "KeyR", 0x52, 0);
            case GLFW.GLFW_KEY_S -> new KeyEventInfo("s", "S", "KeyS", 0x53, 0);
            case GLFW.GLFW_KEY_T -> new KeyEventInfo("t", "T", "KeyT", 0x54, 0);
            case GLFW.GLFW_KEY_U -> new KeyEventInfo("u", "U", "KeyU", 0x55, 0);
            case GLFW.GLFW_KEY_V -> new KeyEventInfo("v", "V", "KeyV", 0x56, 0);
            case GLFW.GLFW_KEY_W -> new KeyEventInfo("w", "W", "KeyW", 0x57, 0);
            case GLFW.GLFW_KEY_X -> new KeyEventInfo("x", "X", "KeyX", 0x58, 0);
            case GLFW.GLFW_KEY_Y -> new KeyEventInfo("y", "Y", "KeyY", 0x59, 0);
            case GLFW.GLFW_KEY_Z -> new KeyEventInfo("z", "Z", "KeyZ", 0x5A, 0);
            case GLFW.GLFW_KEY_0 -> new KeyEventInfo("0", ")", "Digit0", 0x30, 0);
            case GLFW.GLFW_KEY_1 -> new KeyEventInfo("1", "!", "Digit1", 0x31, 0);
            case GLFW.GLFW_KEY_2 -> new KeyEventInfo("2", "@", "Digit2", 0x32, 0);
            case GLFW.GLFW_KEY_3 -> new KeyEventInfo("3", "#", "Digit3", 0x33, 0);
            case GLFW.GLFW_KEY_4 -> new KeyEventInfo("4", "$", "Digit4", 0x34, 0);
            case GLFW.GLFW_KEY_5 -> new KeyEventInfo("5", "%", "Digit5", 0x35, 0);
            case GLFW.GLFW_KEY_6 -> new KeyEventInfo("6", "^", "Digit6", 0x36, 0);
            case GLFW.GLFW_KEY_7 -> new KeyEventInfo("7", "&", "Digit7", 0x37, 0);
            case GLFW.GLFW_KEY_8 -> new KeyEventInfo("8", "*", "Digit8", 0x38, 0);
            case GLFW.GLFW_KEY_9 -> new KeyEventInfo("9", "(", "Digit9", 0x39, 0);

            // --- Function Keys ---
            case GLFW.GLFW_KEY_F1 -> new KeyEventInfo("F1", "F1", "F1", 0x70, 0);
            case GLFW.GLFW_KEY_F2 -> new KeyEventInfo("F2", "F2", "F2", 0x71, 0);
            case GLFW.GLFW_KEY_F3 -> new KeyEventInfo("F3", "F3", "F3", 0x72, 0);
            case GLFW.GLFW_KEY_F4 -> new KeyEventInfo("F4", "F4", "F4", 0x73, 0);
            case GLFW.GLFW_KEY_F5 -> new KeyEventInfo("F5", "F5", "F5", 0x74, 0);
            case GLFW.GLFW_KEY_F6 -> new KeyEventInfo("F6", "F6", "F6", 0x75, 0);
            case GLFW.GLFW_KEY_F7 -> new KeyEventInfo("F7", "F7", "F7", 0x76, 0);
            case GLFW.GLFW_KEY_F8 -> new KeyEventInfo("F8", "F8", "F8", 0x77, 0);
            case GLFW.GLFW_KEY_F9 -> new KeyEventInfo("F9", "F9", "F9", 0x78, 0);
            case GLFW.GLFW_KEY_F10 -> new KeyEventInfo("F10", "F10", "F10", 0x79, 0);
            case GLFW.GLFW_KEY_F11 -> new KeyEventInfo("F11", "F11", "F11", 0x7A, 0);
            case GLFW.GLFW_KEY_F12 -> new KeyEventInfo("F12", "F12", "F12", 0x7B, 0);

            // --- Modifier Keys ---
            case GLFW.GLFW_KEY_LEFT_SHIFT -> new KeyEventInfo("Shift", "Shift", "ShiftLeft", 0x10, 1);
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> new KeyEventInfo("Shift", "Shift", "ShiftRight", 0x10, 2);
            case GLFW.GLFW_KEY_LEFT_CONTROL -> new KeyEventInfo("Control", "Control", "ControlLeft", 0x11, 1);
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> new KeyEventInfo("Control", "Control", "ControlRight", 0x11, 2);
            case GLFW.GLFW_KEY_LEFT_ALT -> new KeyEventInfo("Alt", "Alt", "AltLeft", 0x12, 1);
            case GLFW.GLFW_KEY_RIGHT_ALT -> new KeyEventInfo("Alt", "Alt", "AltRight", 0x12, 2);
            case GLFW.GLFW_KEY_LEFT_SUPER -> new KeyEventInfo("Meta", "Meta", "MetaLeft", 0x5B, 1);
            case GLFW.GLFW_KEY_RIGHT_SUPER -> new KeyEventInfo("Meta", "Meta", "MetaRight", 0x5C, 2);
            case GLFW.GLFW_KEY_CAPS_LOCK -> new KeyEventInfo("CapsLock", "CapsLock", "CapsLock", 0x14, 0);
            case GLFW.GLFW_KEY_NUM_LOCK -> new KeyEventInfo("NumLock", "NumLock", "NumLock", 0x90, 0);

            // --- Whitespace & Editing Keys ---
            case GLFW.GLFW_KEY_ENTER -> new KeyEventInfo("Enter", "Enter", "Enter", 0x0D, 0);
            case GLFW.GLFW_KEY_TAB -> new KeyEventInfo("Tab", "Tab", "Tab", 0x09, 0);
            case GLFW.GLFW_KEY_SPACE -> new KeyEventInfo(" ", " ", "Space", 0x20, 0);
            case GLFW.GLFW_KEY_BACKSPACE -> new KeyEventInfo("Backspace", "Backspace", "Backspace", 0x08, 0);
            case GLFW.GLFW_KEY_DELETE -> new KeyEventInfo("Delete", "Delete", "Delete", 0x2E, 0);
            case GLFW.GLFW_KEY_INSERT -> new KeyEventInfo("Insert", "Insert", "Insert", 0x2D, 0);

            // --- Navigation Keys ---
            case GLFW.GLFW_KEY_ESCAPE -> new KeyEventInfo("Escape", "Escape", "Escape", 0x1B, 0);
            case GLFW.GLFW_KEY_UP -> new KeyEventInfo("ArrowUp", "ArrowUp", "ArrowUp", 0x26, 0);
            case GLFW.GLFW_KEY_DOWN -> new KeyEventInfo("ArrowDown", "ArrowDown", "ArrowDown", 0x28, 0);
            case GLFW.GLFW_KEY_LEFT -> new KeyEventInfo("ArrowLeft", "ArrowLeft", "ArrowLeft", 0x25, 0);
            case GLFW.GLFW_KEY_RIGHT -> new KeyEventInfo("ArrowRight", "ArrowRight", "ArrowRight", 0x27, 0);
            case GLFW.GLFW_KEY_HOME -> new KeyEventInfo("Home", "Home", "Home", 0x24, 0);
            case GLFW.GLFW_KEY_END -> new KeyEventInfo("End", "End", "End", 0x23, 0);
            case GLFW.GLFW_KEY_PAGE_UP -> new KeyEventInfo("PageUp", "PageUp", "PageUp", 0x21, 0);
            case GLFW.GLFW_KEY_PAGE_DOWN -> new KeyEventInfo("PageDown", "PageDown", "PageDown", 0x22, 0);

            // --- Symbol Keys ---
            case GLFW.GLFW_KEY_GRAVE_ACCENT -> new KeyEventInfo("`", "~", "Backquote", 0xC0, 0);
            case GLFW.GLFW_KEY_MINUS -> new KeyEventInfo("-", "_", "Minus", 0xBD, 0);
            case GLFW.GLFW_KEY_EQUAL -> new KeyEventInfo("=", "+", "Equal", 0xBB, 0);
            case GLFW.GLFW_KEY_LEFT_BRACKET -> new KeyEventInfo("[", "{", "BracketLeft", 0xDB, 0);
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> new KeyEventInfo("]", "}", "BracketRight", 0xDD, 0);
            case GLFW.GLFW_KEY_BACKSLASH -> new KeyEventInfo("\\", "|", "Backslash", 0xDC, 0);
            case GLFW.GLFW_KEY_SEMICOLON -> new KeyEventInfo(";", ":", "Semicolon", 0xBA, 0);
            case GLFW.GLFW_KEY_APOSTROPHE -> new KeyEventInfo("'", "\"", "Quote", 0xDE, 0);
            case GLFW.GLFW_KEY_COMMA -> new KeyEventInfo(",", "<", "Comma", 0xBC, 0);
            case GLFW.GLFW_KEY_PERIOD -> new KeyEventInfo(".", ">", "Period", 0xBE, 0);
            case GLFW.GLFW_KEY_SLASH -> new KeyEventInfo("/", "?", "Slash", 0xBF, 0);

            // --- Numpad (Keypad) Keys ---
            case GLFW.GLFW_KEY_KP_0 -> new KeyEventInfo("0", "0", "Numpad0", 0x60, 3);
            case GLFW.GLFW_KEY_KP_1 -> new KeyEventInfo("1", "1", "Numpad1", 0x61, 3);
            case GLFW.GLFW_KEY_KP_2 -> new KeyEventInfo("2", "2", "Numpad2", 0x62, 3);
            case GLFW.GLFW_KEY_KP_3 -> new KeyEventInfo("3", "3", "Numpad3", 0x63, 3);
            case GLFW.GLFW_KEY_KP_4 -> new KeyEventInfo("4", "4", "Numpad4", 0x64, 3);
            case GLFW.GLFW_KEY_KP_5 -> new KeyEventInfo("5", "5", "Numpad5", 0x65, 3);
            case GLFW.GLFW_KEY_KP_6 -> new KeyEventInfo("6", "6", "Numpad6", 0x66, 3);
            case GLFW.GLFW_KEY_KP_7 -> new KeyEventInfo("7", "7", "Numpad7", 0x67, 3);
            case GLFW.GLFW_KEY_KP_8 -> new KeyEventInfo("8", "8", "Numpad8", 0x68, 3);
            case GLFW.GLFW_KEY_KP_9 -> new KeyEventInfo("9", "9", "Numpad9", 0x69, 3);
            case GLFW.GLFW_KEY_KP_DECIMAL -> new KeyEventInfo(".", ".", "NumpadDecimal", 0x6E, 3);
            case GLFW.GLFW_KEY_KP_DIVIDE -> new KeyEventInfo("/", "/", "NumpadDivide", 0x6F, 3);
            case GLFW.GLFW_KEY_KP_MULTIPLY -> new KeyEventInfo("*", "*", "NumpadMultiply", 0x6A, 3);
            case GLFW.GLFW_KEY_KP_SUBTRACT -> new KeyEventInfo("-", "-", "NumpadSubtract", 0x6D, 3);
            case GLFW.GLFW_KEY_KP_ADD -> new KeyEventInfo("+", "+", "NumpadAdd", 0x6B, 3);
            case GLFW.GLFW_KEY_KP_ENTER -> new KeyEventInfo("Enter", "Enter", "NumpadEnter", 0x0D, 3);
            case GLFW.GLFW_KEY_KP_EQUAL -> new KeyEventInfo("=", "=", "NumpadEqual", 0x0C, 3);

            default -> null; // Return null for unhandled keys
        };
    }
}