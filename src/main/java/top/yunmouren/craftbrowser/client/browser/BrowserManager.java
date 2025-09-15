package top.yunmouren.craftbrowser.client.browser;

import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import io.webfolder.cdp.session.SessionInfo;
import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.type.constant.MouseButtonType;
import io.webfolder.cdp.type.constant.MouseEventType;
import org.lwjgl.glfw.GLFW;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.client.BrowserProcess;
import top.yunmouren.craftbrowser.client.config.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrowserManager {
    public static BrowserManager Instance = new BrowserManager();
    private Session session;

    // Helper record to store all necessary key event information
    private record KeyEventInfo(
            String key,                 // e.g., "a"
            String shiftKey,            // e.g., "A"
            String code,                // e.g., "KeyA"
            int windowsVirtualKeyCode,
            int location
    ) {
    }

    public BrowserManager() {
        if (Config.CLIENT.customizeBrowserPortEnabled.get()){
            init("127.0.0.1", Config.CLIENT.customizeBrowserPort.get());
        }else {
            init("127.0.0.1", BrowserProcess.BrowserPort);
        }
    }

    public BrowserManager(String host, int port) {
        init(host, port);
    }

    private void init(String host, int port) {
        try {
            Launcher launcher = new Launcher(new SessionFactory(host, port));
            List<SessionInfo> pages = launcher.factory.list();
            if (!pages.isEmpty()) {
                SessionInfo info = pages.get(0);
                session = launcher.factory.connect(info.getId());
                Craftbrowser.LOGGER.error("Connected to browser page: {}", info.getUrl());
            } else {
                Craftbrowser.LOGGER.error("No pages found on debug port {}", port);
            }
        } catch (Exception e) {
            Craftbrowser.LOGGER.error(e.getMessage());
        }
    }

    public Session getSession() {
        return session;
    }


    public void resizeViewport(int width, int height) {
        if (session == null) return;
        Map<String, Object> params = Map.of(
                "width", width,
                "height", height,
                "deviceScaleFactor", 1.0,
                "mobile", false
        );
        session.send("Emulation.setDeviceMetricsOverride", params);
    }

    // ---------------- Mouse Events (Unchanged) ----------------
    public void mouseMove(int x, int y, boolean dragging) {
        if (session != null) {
            // 如果拖拽，button 填上按下的键；否则 None
            MouseButtonType buttonType = dragging ? MouseButtonType.Left : MouseButtonType.None;

            Map<String, Object> params = Map.of(
                    "type", MouseEventType.MouseMoved, // 仍然是 MouseMoved
                    "x", x,
                    "y", y,
                    "modifiers", 0,
                    "button", buttonType,
                    "clickCount", 0
            );

            session.send("Input.dispatchMouseEvent", params);
        }
    }


    public void mousePress(int x, int y, int button) {
        if (session != null) {
            Map<String, Object> params = Map.of(
                    "type", MouseEventType.MousePressed, "x", x, "y", y,
                    "modifiers", 0, "button", mapButton(button), "clickCount", 1
            );
            session.send("Input.dispatchMouseEvent", params);
        }
    }

    public void mouseRelease(int x, int y, int button) {
        if (session != null) {
            Map<String, Object> params = Map.of(
                    "type", MouseEventType.MouseReleased, "x", x, "y", y,
                    "modifiers", 0, "button", mapButton(button), "clickCount", 1
            );
            session.send("Input.dispatchMouseEvent", params);
        }
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

    // ---------------- Keyboard Events (Updated) ----------------


    /**
     * Handles raw key press and release events, including modifiers and repeats.
     *
     * @param glfwKeyCode   The key code from GLFW.
     * @param glfwModifiers A bitmask of active modifier keys (Shift, Ctrl, etc.) from GLFW.
     * @param isRelease     True if the event is a key release (keyUp).
     * @param isRepeat      True if the event is a key-hold repeat.
     */
    public void keyPress(int glfwKeyCode, int glfwModifiers, boolean isRelease, boolean isRepeat) {
        if (session == null) return;

        KeyEventInfo info = mapGlfwToKeyEventInfo(glfwKeyCode);
        if (info == null) {
            System.err.println("Unhandled GLFW key code: " + glfwKeyCode);
            return;
        }

        // 1. Determine modifier states from the bitmask
        boolean shiftDown = (glfwModifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        boolean capsLockOn = (glfwModifiers & GLFW.GLFW_MOD_CAPS_LOCK) != 0;

        // 2. Determine the effective key value based on modifiers
        String key = shiftDown ? info.shiftKey() : info.key();

        // For letters, CapsLock state XORs with Shift state.
        // e.g., caps on + 'a' -> 'A'
        // e.g., caps on + shift + 'a' -> 'a'
        if (info.key().length() == 1 && Character.isLetter(info.key().charAt(0))) {
            if (capsLockOn ^ shiftDown) { // XOR logic correctly handles both cases
                key = info.shiftKey();
            } else {
                key = info.key();
            }
        }

        // 3. Build the CDP payload
        Map<String, Object> params = new HashMap<>();
        params.put("modifiers", mapGlfwModifiersToCdp(glfwModifiers));
        params.put("windowsVirtualKeyCode", info.windowsVirtualKeyCode());
        params.put("nativeVirtualKeyCode", info.windowsVirtualKeyCode());
        params.put("code", info.code());
        params.put("key", key);
        params.put("isKeypad", info.location() == 3); // Location 3 is the Numpad
        params.put("location", info.location());

        if (!isRelease) {
            // -------- keyDown --------
            params.put("type", "keyDown");
            params.put("autoRepeat", isRepeat); // Set autoRepeat based on the flag

            // 'text' is the character that should be inserted into a text field.
            // It should only be sent for non-repeat, printable keys.
            if (isPrintableKey(key)) {
                params.put("text", key);
                // 'unmodifiedText' is the character without any modifiers.
                params.put("unmodifiedText", info.key());
            }
        } else {
            // -------- keyUp --------
            params.put("type", "keyUp");
            // For keyUp, autoRepeat should always be false.
            params.put("autoRepeat", false);
        }

        // 4. Send the command to the browser
        session.send("Input.dispatchKeyEvent", params);
    }


    // ---------------- Page Control (Unchanged) ----------------
    public void loadUrl(String url) {
        if (session == null || url == null || url.isEmpty()) return;
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        session.send("Page.navigate", Map.of("url", url));
    }
    public void customizeLoadingScreenUrl(){
            if (Config.CLIENT.customizeLoadingScreenEnabled.get()){
                 this.loadCustomizeURL(Config.CLIENT.customizeLoadingScreenUrl.get());
            }else {
                this.loadCustomizeURL("https://example.com");
            }
    }
    public void loadCustomizeURL(String url) {
        if (session == null || url == null || url.isEmpty()) return;
        session.send("Page.navigate", Map.of("url", url));
    }

    public void onClose() {
        if (session != null) {
            session.close();
        }
    }

    /**
     * Handles character input events, ideal for text fields.
     *
     * @param text          The final character generated after all modifiers.
     * @param glfwModifiers A bitmask of active modifier keys.
     */
    public void keyChar(String text, int glfwModifiers) {
        if (session == null || text == null || text.isEmpty()) return;

        Map<String, Object> params = new HashMap<>();
        params.put("type", "char");
        params.put("text", text);
        params.put("unmodifiedText", text.toLowerCase()); // Best guess for unmodified
        params.put("modifiers", mapGlfwModifiersToCdp(glfwModifiers));

        session.send("Input.dispatchKeyEvent", params);
    }

    private boolean isPrintableKey(String key) {
        return key != null && key.length() == 1;
    }

    /**
     * Converts the GLFW modifier bitmask to the CDP modifier bitmask.
     */
    private int mapGlfwModifiersToCdp(int glfwModifiers) {
        int cdpModifiers = 0;
        if ((glfwModifiers & GLFW.GLFW_MOD_ALT) != 0) cdpModifiers |= 1;
        if ((glfwModifiers & GLFW.GLFW_MOD_CONTROL) != 0) cdpModifiers |= 2;
        if ((glfwModifiers & GLFW.GLFW_MOD_SUPER) != 0) cdpModifiers |= 4; // Meta/Win key
        if ((glfwModifiers & GLFW.GLFW_MOD_SHIFT) != 0) cdpModifiers |= 8;
        if ((glfwModifiers & GLFW.GLFW_MOD_CAPS_LOCK) != 0) cdpModifiers |= 16;
        if ((glfwModifiers & GLFW.GLFW_MOD_NUM_LOCK) != 0) cdpModifiers |= 32;
        return cdpModifiers;
    }

    /**
     * Maps a GLFW key code to a comprehensive KeyEventInfo object required by CDP.
     */
    private KeyEventInfo mapGlfwToKeyEventInfo(int keyCode) {
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