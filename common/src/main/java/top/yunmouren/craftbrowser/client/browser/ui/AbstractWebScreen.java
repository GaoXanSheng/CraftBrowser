package top.yunmouren.craftbrowser.client.browser.ui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import top.yunmouren.craftbrowser.client.browser.Controller.TestController;
import top.yunmouren.craftbrowser.client.browser.Tools.CursorType;
import top.yunmouren.craftbrowser.client.browser.api.BrowserAPI;
import top.yunmouren.craftbrowser.client.browser.Controller.IBrowserController;
import top.yunmouren.craftbrowser.client.browser.Core.BrowserRender;
import top.yunmouren.craftbrowser.client.config.Config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import static org.lwjgl.glfw.GLFW.*;

public abstract class AbstractWebScreen extends Screen {
    private final Minecraft mc = Minecraft.getInstance();
    private final Map<Integer, Long> heldKeys = new HashMap<>();
    public BrowserRender browserRender;
    public IBrowserController browserController;

    private Window getWindow() {
        return mc.getWindow();
    }

    protected AbstractWebScreen(Component p_96550_, String url) {
        super(p_96550_);
        browserController = BrowserAPI.getInstance().createBrowser(url, 1920, 1080, 60);
        browserRender = BrowserAPI.getInstance().GetBrowserRender(browserController);
        BrowserResize();

        BrowserAPI.getInstance().GetBrowserEventBus(browserController).register(new TestController());
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingResizeTask = null;

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        BrowserResize();
    }

    public void BrowserResize() {
        if (pendingResizeTask != null && !pendingResizeTask.isDone()) {
            pendingResizeTask.cancel(false);
        }
        int RESIZE_DELAY_MS = 200;

        pendingResizeTask = scheduler.schedule(() -> minecraft.execute(() -> {
            int physWidth = getWindow().getScreenWidth();
            int physHeight = getWindow().getScreenHeight();
            double scale = getWindow().getGuiScale();
            browserController.Resize(physWidth, physHeight, (int) scale, false);
        }), RESIZE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int physWidth = getWindow().getScreenWidth();
        int physHeight = getWindow().getScreenHeight();
        var render = browserRender.render(physWidth, physHeight);
        if (render == 0) return;

        RenderSystem.disableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, render);

        float maxU = browserRender.getValidU();
        float maxV = browserRender.getValidV();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        float guiScale = (float) getWindow().getGuiScale();
        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.scale(1.0f / guiScale, 1.0f / guiScale, 1.0f);
        Matrix4f matrix = poseStack.last().pose();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, 0, physHeight, 0).uv(0f, maxV).endVertex();
        buffer.vertex(matrix, physWidth, physHeight, 0).uv(maxU, maxV).endVertex();
        buffer.vertex(matrix, physWidth, 0, 0).uv(maxU, 0f).endVertex();
        buffer.vertex(matrix, 0, 0, 0).uv(0f, 0f).endVertex();

        tessellator.end();
        poseStack.popPose();
        RenderSystem.enableDepthTest();
        updateCursor();
    }

    public static int[] guiToPixel(double guiX, double guiY) {
        Minecraft mc = Minecraft.getInstance();
        int windowWidth = mc.getWindow().getScreenWidth();
        int windowHeight = mc.getWindow().getScreenHeight();
        int guiWidth = mc.getWindow().getGuiScaledWidth();
        int guiHeight = mc.getWindow().getGuiScaledHeight();
        int pixelX = (int) (guiX * ((double) windowWidth / guiWidth));
        int pixelY = (int) (guiY * ((double) windowHeight / guiHeight));

        return new int[]{pixelX, pixelY};
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        CompletableFuture.runAsync(() -> {
            int[] pos = guiToPixel(mouseX, mouseY);
            boolean dragging = !heldMouseButtons.isEmpty();
            browserController.SendMouseMove(pos[0], pos[1], dragging);
        });
    }

    private final Set<Integer> heldMouseButtons = new HashSet<>();

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int[] pos = guiToPixel(mouseX, mouseY);
        browserController.SendMouseClick(pos[0], pos[1], button, false);
        heldMouseButtons.add(button);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int[] pos = guiToPixel(mouseX, mouseY);
        browserController.SendMouseClick(pos[0], pos[1], button, true);
        heldMouseButtons.remove(button);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int[] pos = guiToPixel(mouseX, mouseY);
        int scrollAmountY = (int) (delta * Config.CLIENT.scrollWheelPixels.get());
        browserController.SendMouseWheel(pos[0], pos[1], 0, scrollAmountY);
        return true;
    }

    private int cachedPhysWidth = -1;
    private int cachedPhysHeight = -1;

    @Override
    public void tick() {
        super.tick();
        long now = System.currentTimeMillis();
        for (Map.Entry<Integer, Long> entry : heldKeys.entrySet()) {
            if (now - entry.getValue() >= Config.CLIENT.keyPressDelay.get()) {
                int glfwKey = entry.getKey();
                int winKeyCode = getWindowsKeyCode(glfwKey);
                browserController.SendKeyEvent(winKeyCode, true);
                entry.setValue(now);
            }
        }
        Window window = getWindow();
        int physWidth = window.getScreenWidth();
        int physHeight = window.getScreenHeight();
        if (physWidth != cachedPhysWidth || physHeight != cachedPhysHeight) {
            cachedPhysWidth = physWidth;
            cachedPhysHeight = physHeight;
            this.BrowserResize();
        }
    }

    /**
     * 1-9
     * A-Z
     * esc
     * enter
     * backspace
     */
    public int getWindowsKeyCode(int glfwKey) {
        return switch (glfwKey) {
            case 257 -> 13; // GLFW_KEY_ENTER -> VK_RETURN
            case 256 -> 27; // GLFW_KEY_ESCAPE -> VK_ESCAPE
            case 259 -> 8;  // GLFW_KEY_BACKSPACE -> VK_BACK
            default -> glfwKey;
        };
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW_KEY_ESCAPE && (modifiers & GLFW_MOD_SHIFT) != 0) {
            this.onClose();
            return true;
        }
        browserController.SendKeyEvent(getWindowsKeyCode(keyCode), false);
        heldKeys.put(keyCode, System.currentTimeMillis());
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW_KEY_ESCAPE && (modifiers & GLFW_MOD_SHIFT) != 0) {
            this.onClose();
            return true;
        }
        browserController.SendKeyEvent(getWindowsKeyCode(keyCode), true);
        heldKeys.remove(keyCode);
        return true;
    }

    @Override
    public void onClose() {
        heldKeys.clear();
        long window = mc.getWindow().getWindow();
        glfwSetCursor(window, glfwCreateStandardCursor(GLFW_ARROW_CURSOR));
        browserRender.close();
        BrowserAPI.getInstance().removeBrowser(browserController);
        Minecraft.getInstance().setScreen(null);
    }

    private int lastCursorType;

    private void updateCursor() {
        var currentCursorIndex = CursorType.values()[browserController.GetCursorType()];
        int targetGlfwCursor = CursorType.fromBrowserInt(currentCursorIndex);
        if (targetGlfwCursor != lastCursorType) {
            lastCursorType = targetGlfwCursor;
            long window = mc.getWindow().getWindow();
            glfwSetCursor(window, glfwCreateStandardCursor(targetGlfwCursor));
        }
    }
}