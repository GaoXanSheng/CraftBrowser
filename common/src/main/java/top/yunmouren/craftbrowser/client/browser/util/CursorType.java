package top.yunmouren.craftbrowser.client.browser.util;

import org.lwjgl.glfw.GLFW;

public enum CursorType {
    POINTER(GLFW.GLFW_HAND_CURSOR),          // 手型（链接）
    CROSS(GLFW.GLFW_CROSSHAIR_CURSOR),       // 十字
    HAND(GLFW.GLFW_HAND_CURSOR),             // 手型
    IBEAM(GLFW.GLFW_IBEAM_CURSOR),           // I型（文本）
    WAIT(GLFW.GLFW_ARROW_CURSOR),            // 等待（GLFW 不支持，使用箭头）
    HELP(GLFW.GLFW_ARROW_CURSOR),            // 帮助（GLFW 不支持，使用箭头）
    EAST_RESIZE(GLFW.GLFW_HRESIZE_CURSOR),   // 东向调整大小
    NORTH_RESIZE(GLFW.GLFW_VRESIZE_CURSOR),  // 北向调整大小
    NORTHEAST_RESIZE(GLFW.GLFW_ARROW_CURSOR),// 东北向（GLFW 不支持）
    NORTHWEST_RESIZE(GLFW.GLFW_ARROW_CURSOR),// 西北向（GLFW 不支持）
    SOUTH_RESIZE(GLFW.GLFW_VRESIZE_CURSOR),  // 南向调整大小
    SOUTHEAST_RESIZE(GLFW.GLFW_ARROW_CURSOR),// 东南向（GLFW 不支持）
    SOUTHWEST_RESIZE(GLFW.GLFW_ARROW_CURSOR),// 西南向（GLFW 不支持）
    WEST_RESIZE(GLFW.GLFW_HRESIZE_CURSOR),   // 西向调整大小
    NORTH_SOUTH_RESIZE(GLFW.GLFW_VRESIZE_CURSOR),  // 南北调整大小
    EAST_WEST_RESIZE(GLFW.GLFW_HRESIZE_CURSOR),    // 东西调整大小
    NORTHEAST_SOUTHWEST_RESIZE(GLFW.GLFW_ARROW_CURSOR), // 东北-西南（GLFW 不支持）
    NORTHWEST_SOUTHEAST_RESIZE(GLFW.GLFW_ARROW_CURSOR), // 西北-东南（GLFW 不支持）
    COLUMN_RESIZE(GLFW.GLFW_HRESIZE_CURSOR), // 列调整大小
    ROW_RESIZE(GLFW.GLFW_VRESIZE_CURSOR),    // 行调整大小
    MIDDLE_PANNING(GLFW.GLFW_ARROW_CURSOR),  // 中键平移
    EAST_PANNING(GLFW.GLFW_ARROW_CURSOR),    // 东向平移
    NORTH_PANNING(GLFW.GLFW_ARROW_CURSOR),   // 北向平移
    NORTHEAST_PANNING(GLFW.GLFW_ARROW_CURSOR),// 东北向平移
    NORTHWEST_PANNING(GLFW.GLFW_ARROW_CURSOR),// 西北向平移
    SOUTH_PANNING(GLFW.GLFW_ARROW_CURSOR),   // 南向平移
    SOUTHEAST_PANNING(GLFW.GLFW_ARROW_CURSOR),// 东南向平移
    SOUTHWEST_PANNING(GLFW.GLFW_ARROW_CURSOR),// 西南向平移
    WEST_PANNING(GLFW.GLFW_ARROW_CURSOR),    // 西向平移
    MOVE(GLFW.GLFW_ARROW_CURSOR),            // 移动
    VERTICAL_TEXT(GLFW.GLFW_IBEAM_CURSOR),   // 垂直文本
    CELL(GLFW.GLFW_ARROW_CURSOR),            // 单元格
    CONTEXT_MENU(GLFW.GLFW_ARROW_CURSOR),    // 上下文菜单
    ALIAS(GLFW.GLFW_ARROW_CURSOR),           // 别名
    PROGRESS(GLFW.GLFW_ARROW_CURSOR),        // 进度
    NO_DROP(GLFW.GLFW_ARROW_CURSOR),         // 禁止拖放
    COPY(GLFW.GLFW_ARROW_CURSOR),            // 复制
    NONE(GLFW.GLFW_ARROW_CURSOR),            // 无
    NOT_ALLOWED(GLFW.GLFW_ARROW_CURSOR),     // 不允许
    ZOOM_IN(GLFW.GLFW_ARROW_CURSOR),         // 放大
    ZOOM_OUT(GLFW.GLFW_ARROW_CURSOR),        // 缩小
    GRAB(GLFW.GLFW_HAND_CURSOR),             // 抓取
    GRABBING(GLFW.GLFW_HAND_CURSOR),         // 抓取中
    CUSTOM(GLFW.GLFW_ARROW_CURSOR),          // 自定义
    DEFAULT(GLFW.GLFW_ARROW_CURSOR);         // 默认箭头

    private final int glfwCursor;

    CursorType(int glfwCursor) {
        this.glfwCursor = glfwCursor;
    }

    public int getGlfwCursor() {
        return glfwCursor;
    }

    /**
     * 从 CSS 光标类型字符串转换为枚举
     * 参考: https://developer.mozilla.org/en-US/docs/Web/CSS/cursor
     */
    public static CursorType fromCssValue(String cssValue) {
        if (cssValue == null || cssValue.isEmpty()) return DEFAULT;

        // CSS 光标值使用连字符，转换为枚举匹配
        String normalized = cssValue.toLowerCase().trim();

        return switch (normalized) {
            case "pointer", "hand" -> POINTER;
            case "crosshair" -> CROSS;
            case "text", "i-beam" -> IBEAM;
            case "wait" -> WAIT;
            case "help" -> HELP;
            case "e-resize" -> EAST_RESIZE;
            case "n-resize" -> NORTH_RESIZE;
            case "ne-resize" -> NORTHEAST_RESIZE;
            case "nw-resize" -> NORTHWEST_RESIZE;
            case "s-resize" -> SOUTH_RESIZE;
            case "se-resize" -> SOUTHEAST_RESIZE;
            case "sw-resize" -> SOUTHWEST_RESIZE;
            case "w-resize" -> WEST_RESIZE;
            case "ns-resize" -> NORTH_SOUTH_RESIZE;
            case "ew-resize" -> EAST_WEST_RESIZE;
            case "nesw-resize" -> NORTHEAST_SOUTHWEST_RESIZE;
            case "nwse-resize" -> NORTHWEST_SOUTHEAST_RESIZE;
            case "col-resize" -> COLUMN_RESIZE;
            case "row-resize" -> ROW_RESIZE;
            case "all-scroll" -> MIDDLE_PANNING;
            case "move" -> MOVE;
            case "vertical-text" -> VERTICAL_TEXT;
            case "cell" -> CELL;
            case "context-menu" -> CONTEXT_MENU;
            case "alias" -> ALIAS;
            case "progress" -> PROGRESS;
            case "no-drop" -> NO_DROP;
            case "copy" -> COPY;
            case "none" -> NONE;
            case "not-allowed" -> NOT_ALLOWED;
            case "zoom-in" -> ZOOM_IN;
            case "zoom-out" -> ZOOM_OUT;
            case "grab" -> GRAB;
            case "grabbing" -> GRABBING;
            default -> DEFAULT;
        };
    }
}

