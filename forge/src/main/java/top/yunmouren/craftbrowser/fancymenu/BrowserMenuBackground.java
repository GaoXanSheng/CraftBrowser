package top.yunmouren.craftbrowser.fancymenu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.keksuccino.fancymenu.customization.background.MenuBackground;
import de.keksuccino.fancymenu.customization.background.MenuBackgroundBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import top.yunmouren.craftbrowser.client.browser.BrowserManager;
import top.yunmouren.craftbrowser.client.browser.BrowserRender;
import top.yunmouren.craftbrowser.client.config.Config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BrowserMenuBackground extends MenuBackground {
    private final Minecraft mc = Minecraft.getInstance();
    private int texWidth = mc.getWindow().getScreenWidth();
    private int texHeight = mc.getWindow().getScreenHeight();
    private final BrowserManager browserManager = new BrowserManager();
    private final BrowserRender browserRender = new BrowserRender();
    public String url;

    public BrowserMenuBackground(MenuBackgroundBuilder<?> builder) {
        super(builder);
        browserManager.resizeViewport(texWidth, texHeight);
        browserManager.customizeLoadingScreenUrl();
    }
    protected BrowserMenuBackground(MenuBackgroundBuilder<?> builder, @NotNull String url){
        super(builder);
        browserManager.resizeViewport(texWidth, texHeight);
        loadUrl(url);
    }
    public void loadUrl(String url) {
            this.url = url;
            if (url.startsWith("[source:web]")) {
                url = url.substring("[source:web]".length());
            }
            browserManager.loadUrl(url);
    }
    public void customizeLoadingScreenUrl(){
        browserManager.customizeLoadingScreenUrl();
    }
    @Override
    public boolean isFocusable() {
        return true;
    }
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        var render = browserRender.render(texWidth, texHeight);
        if (render == 0) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, render);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        Matrix4f matrix = graphics.pose().last().pose();
        int guiWidth = mc.getWindow().getGuiScaledWidth();
        int guiHeight = mc.getWindow().getGuiScaledHeight();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, 0, guiHeight, 0).uv(0, 1).endVertex();
        buffer.vertex(matrix, guiWidth, guiHeight, 0).uv(1, 1).endVertex();
        buffer.vertex(matrix, guiWidth, 0, 0).uv(1, 0).endVertex();
        buffer.vertex(matrix, 0, 0, 0).uv(0, 0).endVertex();
        tessellator.end();
    }
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingResizeTask = null;

    @Override
    public void onAfterResizeScreen() {
        if (pendingResizeTask != null && !pendingResizeTask.isDone()) {
            pendingResizeTask.cancel(false);
        }
        int RESIZE_DELAY_MS = 200;
        pendingResizeTask = scheduler.schedule(() -> {
            this.texWidth = mc.getWindow().getScreenWidth();
            this.texHeight = mc.getWindow().getScreenHeight();
            browserManager.resizeViewport(texWidth, texHeight);
        }, RESIZE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    public static int[] guiToPixel(double guiX, double guiY) {
        Minecraft mc = Minecraft.getInstance();
        int windowWidth = mc.getWindow().getScreenWidth();   // 实际像素宽
        int windowHeight = mc.getWindow().getScreenHeight(); // 实际像素高
        int guiWidth = mc.getWindow().getGuiScaledWidth();   // GUI 逻辑宽
        int guiHeight = mc.getWindow().getGuiScaledHeight(); // GUI 逻辑高

        int pixelX = (int) (guiX * ((double) windowWidth / guiWidth));
        int pixelY = (int) (guiY * ((double) windowHeight / guiHeight));

        return new int[]{pixelX, pixelY};
    }
    private final Set<Integer> heldMouseButtons = new HashSet<>();
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        int[] pos = guiToPixel(mouseX, mouseY);
        boolean dragging = !heldMouseButtons.isEmpty();
        browserManager.mouseMove(pos[0], pos[1], dragging);
        super.mouseMoved(mouseX, mouseY);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int[] pos = guiToPixel(mouseX, mouseY);
        browserManager.mousePress(pos[0], pos[1], button);
        heldMouseButtons.add(button); // 记录按下
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int[] pos = guiToPixel(mouseX, mouseY);
        browserManager.mouseRelease(pos[0], pos[1], button);
        heldMouseButtons.remove(button); // 移除按下记录
        return super.mouseReleased(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int[] pos = guiToPixel(mouseX, mouseY);
        browserManager.mouseWheel(pos[0], pos[1], (int) (-delta * Config.CLIENT.scrollWheelPixels.get()));
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
    private final Map<Integer, Long> heldKeys = new HashMap<>();
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        browserManager.keyPress(keyCode, modifiers, false, false);
        heldKeys.put(keyCode, System.currentTimeMillis());
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        browserManager.keyPress(keyCode, modifiers, true, false);
        heldKeys.remove(keyCode);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }
}
