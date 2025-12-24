package top.yunmouren.craftbrowser.client.browser.ui;

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
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
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


    protected AbstractWebScreen(Component p_96550_) {
        super(p_96550_);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void tick() {
        super.tick();
        long now = System.currentTimeMillis();
        for (Map.Entry<Integer, Long> entry : heldKeys.entrySet()) {
            int keyCode = entry.getKey();
            long lastTime = entry.getValue();
            if (now - lastTime >= Config.CLIENT.keyPressDelay.get()) {
                browserManager.getKeyHandler().keyPress(keyCode, 0, false, true); // repeat
                entry.setValue(now);
            }
        }

    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int pixelW = mc.getWindow().getScreenWidth();
        int pixelH = mc.getWindow().getScreenHeight();
        ResourceLocation render = browserRender.render(pixelW, pixelH);
        if (render == null) return;
        updateCursor();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, render);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        int guiW = mc.getWindow().getGuiScaledWidth();
        int guiH = mc.getWindow().getGuiScaledHeight();
        guiGraphics.blit(
                render,
                0, 0,               // 屏幕上的位置 (X, Y)
                guiW, guiH,         // 屏幕上的大小 (宽, 高) -> 适配 GUI
                0.0F, 0.0F,         // 纹理起始 UV
                pixelW, pixelH,     // 纹理采样大小 (采样整张高清图)
                pixelW, pixelH      // 纹理总大小
        );
    }



    private void updateCursor() {
        CursorType currentCursor = browserManager.getCurrentCursor();
        if (currentCursor != lastCursorType) {
            long window = mc.getWindow().getWindow();
            glfwSetCursor(window, glfwCreateStandardCursor(currentCursor.getGlfwCursor()));
            lastCursorType = currentCursor;
        }
    }


    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingResizeTask = null;

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);

        if (pendingResizeTask != null && !pendingResizeTask.isDone()) {
            pendingResizeTask.cancel(false);
        }

        pendingResizeTask = scheduler.schedule(() -> {
            int pixelW = mc.getWindow().getScreenWidth();
            int pixelH = mc.getWindow().getScreenHeight();
            browserManager.getPageHandler().resizeViewport(pixelW, pixelH);
        }, 200, TimeUnit.MILLISECONDS);
    }


    public static int[] guiToPixel(double guiX, double guiY) {
        Minecraft mc = Minecraft.getInstance();

        int pixelW = mc.getWindow().getScreenWidth();
        int pixelH = mc.getWindow().getScreenHeight();
        int guiW = mc.getWindow().getGuiScaledWidth();
        int guiH = mc.getWindow().getGuiScaledHeight();

        return new int[] {
                (int) (guiX * pixelW / guiW),
                (int) (guiY * pixelH / guiH)
        };
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
        heldMouseButtons.add(button); // 记录按下
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int[] pos = guiToPixel(mouseX, mouseY);
        browserManager.getMouseHandler().mouseRelease(pos[0], pos[1], button);
        heldMouseButtons.remove(button); // 移除按下记录
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int[] pos = guiToPixel(mouseX, mouseY);
        browserManager.getMouseHandler().mouseWheel(pos[0], pos[1], (int) (-delta * Config.CLIENT.scrollWheelPixels.get()));
        return true;
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

        // 恢复默认光标
        long window = mc.getWindow().getWindow();
        glfwSetCursor(window, glfwCreateStandardCursor(GLFW_ARROW_CURSOR));
        lastCursorType = CursorType.DEFAULT;

        Minecraft.getInstance().setScreen(null);
    }
}
