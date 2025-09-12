package top.yunmouren.craftbrowser.client.browser;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import spout.JNISpout;
import top.yunmouren.craftbrowser.Craftbrowser;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static top.yunmouren.craftbrowser.client.BrowserProcess.SPOUT_ID;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractWebScreen extends Screen {
    private long spoutPtr = 0;
    private DynamicTexture dynTex;
    private final Minecraft mc = Minecraft.getInstance();
    private int texWidth = mc.getWindow().getScreenWidth();
    private int texHeight = mc.getWindow().getScreenHeight();
    private boolean receiverConnected = false;
    public final BrowserManager browserManager = new BrowserManager();
    private final Set<Integer> heldKeys = new HashSet<>();

    protected AbstractWebScreen(Component p_96550_) {
        super(p_96550_);
        if (spoutPtr == 0) {
            spoutPtr = JNISpout.init();
        }
        if (!receiverConnected) {
            int[] dim = new int[]{this.texWidth, this.texHeight};
            receiverConnected = JNISpout.createReceiver("WebViewSpoutCapture_" + SPOUT_ID, dim, spoutPtr);
            if (!receiverConnected) {
                Craftbrowser.LOGGER.error("Failed to create Spout receiver! Is the sender running?");
                return;
            }
        }
        if (dynTex == null) {
            dynTex = new DynamicTexture(this.texWidth, this.texHeight, true);
        }
        browserManager.resizeViewport(texWidth, texHeight);
    }

    @Override
    public void tick() {
        super.tick();
        for (int keyCode : heldKeys) {
            browserManager.keyPress(keyCode, 0, false, true);
        }
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (!receiverConnected || this.dynTex == null) {
            return;
        }
        if (dynTex.getPixels() != null && (dynTex.getPixels().getWidth() != this.texWidth || dynTex.getPixels().getHeight() != this.texHeight)) {
            dynTex.close();
            dynTex = new DynamicTexture(this.texWidth, this.texHeight, true);
        }
        int[] dim = new int[]{this.texWidth, this.texHeight};
        boolean received = JNISpout.receiveTexture(dim, dynTex.getId(), GL11.GL_TEXTURE_2D, false, spoutPtr);

        if (!received) {
            Craftbrowser.LOGGER.error("Spout receiveTexture failed. Releasing receiver.");
            releaseSpout();
            return;
        }
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, dynTex.getId());

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        Matrix4f matrix = poseStack.last().pose();
        int guiWidth = mc.getWindow().getGuiScaledWidth();
        int guiHeight = mc.getWindow().getGuiScaledHeight();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, 0, guiHeight, 0).uv(0, 1).endVertex();
        buffer.vertex(matrix, guiWidth, guiHeight, 0).uv(1, 1).endVertex();
        buffer.vertex(matrix, guiWidth, 0, 0).uv(1, 0).endVertex();
        buffer.vertex(matrix, 0, 0, 0).uv(0, 0).endVertex();
        tessellator.end();

        RenderSystem.enableDepthTest();
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void releaseSpout() {
        if (receiverConnected) {
            JNISpout.releaseReceiver(spoutPtr);
            receiverConnected = false;
        }
        if (spoutPtr != 0) {
            JNISpout.deInit(spoutPtr);
            spoutPtr = 0;
        }
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingResizeTask = null;
    private final int RESIZE_DELAY_MS = 200; // 延迟200毫秒执行

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        // 取消上一次延迟任务（如果仍未执行）
        if (pendingResizeTask != null && !pendingResizeTask.isDone()) {
            pendingResizeTask.cancel(false);
        }
        // 提交新的延迟任务
        pendingResizeTask = scheduler.schedule(() -> {
            this.texWidth = mc.getWindow().getScreenWidth();
            this.texHeight = mc.getWindow().getScreenHeight();
            browserManager.resizeViewport(texWidth, texHeight);
        }, RESIZE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 将 Minecraft GUI 坐标转换为窗口像素坐标
     *
     * @param guiX GUI 坐标 X
     * @param guiY GUI 坐标 Y
     * @return 长度为 2 的数组 [pixelX, pixelY]
     */
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

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        int[] pos = guiToPixel(mouseX, mouseY);
        boolean dragging = !heldMouseButtons.isEmpty();
        browserManager.mouseMove(pos[0], pos[1], dragging);
    }

    private final Set<Integer> heldMouseButtons = new HashSet<>();

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int[] pos = guiToPixel(mouseX, mouseY);
        browserManager.mousePress(pos[0], pos[1], button);
        heldMouseButtons.add(button); // 记录按下
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int[] pos = guiToPixel(mouseX, mouseY);
        browserManager.mouseRelease(pos[0], pos[1], button);
        heldMouseButtons.remove(button); // 移除按下记录
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int[] pos = guiToPixel(mouseX, mouseY);
        browserManager.mouseWheel(pos[0], pos[1], (int) (-delta * 100));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        if (!heldKeys.contains(keyCode)) {
            browserManager.keyPress(keyCode, modifiers, false, false); // isReleased=false, isRepeat=false
            heldKeys.add(keyCode);
        }
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        browserManager.keyPress(keyCode, modifiers, true, false); // isReleased=true
        heldKeys.remove(keyCode);
        return true;
    }

    @Override
    public void onClose() {
        browserManager.loadUrl("https://example.com/");
        heldKeys.clear();
        Minecraft.getInstance().setScreen(null);
    }
}
