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
import top.yunmouren.craftbrowser.client.browser.api.BrowserAPI;
import top.yunmouren.craftbrowser.client.browser.core.BrowserManager;
import top.yunmouren.craftbrowser.client.browser.core.BrowserRender;
import top.yunmouren.craftbrowser.client.browser.util.CursorType;
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
    public final BrowserAPI browser = BrowserAPI.getInstance();
    private CursorType lastCursorType = CursorType.DEFAULT;
    public BrowserRender browserRender = BrowserAPI.getGlobalRender();
    public BrowserManager browserManager = BrowserAPI.getGlobalManager();

    private Window getWindow() {
        return mc.getWindow();
    }

    protected AbstractWebScreen(Component p_96550_) {
        super(p_96550_);
        resize(mc, mc.getWindow().getScreenWidth(), mc.getWindow().getScreenHeight());
    }

    @Override
    protected void init() {
        super.init();
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingResizeTask = null;

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);

        if (pendingResizeTask != null && !pendingResizeTask.isDone()) {
            pendingResizeTask.cancel(false);
        }
        int RESIZE_DELAY_MS = 200;

        pendingResizeTask = scheduler.schedule(() -> {
            minecraft.execute(() -> {
                int physWidth = getWindow().getScreenWidth();
                int physHeight = getWindow().getScreenHeight();
                double scale = getWindow().getGuiScale();
                browserManager.getPageHandler().resizeViewport(physWidth, physHeight, scale);
            });
        }, RESIZE_DELAY_MS, TimeUnit.MILLISECONDS);
    }
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int physWidth = getWindow().getScreenWidth();
        int physHeight = getWindow().getScreenHeight();
        var render = browserRender.render(physWidth, physHeight);
        if (render == 0) return;

        updateCursor();

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
            browserManager.getMouseHandler().mouseMove(pos[0], pos[1], dragging);
            browserManager.updateCursorAtPosition(pos[0], pos[1]);
        });
    }
    private final Set<Integer> heldMouseButtons = new HashSet<>();

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int[] pos = guiToPixel(mouseX, mouseY);
        browserManager.getMouseHandler().mousePress(pos[0], pos[1], button);
        heldMouseButtons.add(button);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int[] pos = guiToPixel(mouseX, mouseY);
        browserManager.getMouseHandler().mouseRelease(pos[0], pos[1], button);
        heldMouseButtons.remove(button);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int[] pos = guiToPixel(mouseX, mouseY);
        browserManager.getMouseHandler().mouseWheel(pos[0], pos[1], (int) (-delta * Config.CLIENT.scrollWheelPixels.get()));
        return true;
    }
    @Override
    public void tick() {
        super.tick();
        long now = System.currentTimeMillis();
        for (Map.Entry<Integer, Long> entry : heldKeys.entrySet()) {
            if (now - entry.getValue() >= Config.CLIENT.keyPressDelay.get()) {
                browserManager.getKeyHandler().keyPress(entry.getKey(), 0, false, true);
                entry.setValue(now);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW_KEY_ESCAPE && (modifiers & GLFW_MOD_SHIFT) != 0) {
            this.onClose();
            return true;
        }
        browserManager.getKeyHandler().keyPress(keyCode, modifiers, false, false);
        heldKeys.put(keyCode, System.currentTimeMillis());
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW_KEY_ESCAPE && (modifiers & GLFW_MOD_SHIFT) != 0) {
            this.onClose();
            return true;
        }
        browserManager.getKeyHandler().keyPress(keyCode, modifiers, true, false);
        heldKeys.remove(keyCode);
        return true;
    }

    @Override
    public void onClose() {
        browserManager.getPageHandler().loadCustomizeURL("about:blank");
        heldKeys.clear();
        long window = mc.getWindow().getWindow();
        glfwSetCursor(window, glfwCreateStandardCursor(GLFW_ARROW_CURSOR));
        lastCursorType = CursorType.DEFAULT;
        Minecraft.getInstance().setScreen(null);
    }

    private void updateCursor() {
        CursorType currentCursor = browserManager.getCurrentCursor();
        if (currentCursor != lastCursorType) {
            long window = mc.getWindow().getWindow();
            glfwSetCursor(window, glfwCreateStandardCursor(currentCursor.getGlfwCursor()));
            lastCursorType = currentCursor;
        }
    }
}