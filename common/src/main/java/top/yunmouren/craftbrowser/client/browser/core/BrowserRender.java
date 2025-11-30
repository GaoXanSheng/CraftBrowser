package top.yunmouren.craftbrowser.client.browser.core;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import spout.JNISpout;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.client.config.Config;

public class BrowserRender extends JNISpout {
    private long spoutPtr = 0;
    private boolean receiverConnected = false;

    private final String currentSpoutID;

    public final int[] dim = new int[2];

    private DynamicTexture dynTex;

    public BrowserRender(String spoutID, int width, int height) {
        super();
        this.currentSpoutID = spoutID;
        this.dim[0] = Math.max(1, width);
        this.dim[1] = Math.max(1, height);
    }

    public BrowserRender() {
        this(Config.CLIENT.customizeSpoutID.get(),
                Minecraft.getInstance().getWindow().getScreenWidth(),
                Minecraft.getInstance().getWindow().getScreenHeight());
    }

    // 将初始化逻辑封装，仅在 Render 线程调用
    private void ensureInitialized() {
        if (this.spoutPtr == 0) {
            try {
                this.spoutPtr = this.init();
                Craftbrowser.LOGGER.info("Spout initialized successfully. Ptr={}", this.spoutPtr);
            } catch (Throwable t) {
                Craftbrowser.LOGGER.error("Fatal: JNISpout init failed", t);
                this.spoutPtr = 0;
            }
        }
    }

    private void initReceiver(String id, int w, int h) {
        ensureInitialized();
        if (spoutPtr == 0 || w <= 0 || h <= 0) return;

        cleanupReceiver();

        // 锁定尺寸状态
        this.dim[0] = w;
        this.dim[1] = h;

        try {
            Craftbrowser.LOGGER.info("Spout Receiver initializing: Name={} Dim={}x{}", id, w, h);

            int[] tempDim = new int[]{w, h};
            receiverConnected = this.createReceiver(id, tempDim, spoutPtr);

        } catch (Throwable t) {
            receiverConnected = false;
            Craftbrowser.LOGGER.error("Spout createReceiver failed", t);
        }

        if (dynTex == null || dynTex.getPixels().getWidth() != w || dynTex.getPixels().getHeight() != h) {
            if (dynTex != null) {
                RenderSystem.bindTexture(0);
                dynTex.close();
            }
            dynTex = new DynamicTexture(w, h, true);
        }
    }

    private void cleanupReceiver() {
        if (spoutPtr != 0 && receiverConnected) {
            try {
                this.releaseReceiver(spoutPtr);
            } catch (Throwable ignored) {
            }
            receiverConnected = false;
        }
    }

    public void receiveTexture(DynamicTexture dynamicTexture) {
        if (spoutPtr == 0 || !receiverConnected || dim == null || dynamicTexture.getId() <= 0) return;
        try {
            this.receiveTexture(dim, dynamicTexture.getId(), GL11.GL_TEXTURE_2D, false, spoutPtr);
        } catch (Throwable ignored) {
        }
    }

    public int render(int width, int height) {
        if (width <= 0 || height <= 0) return 0;
        if (this.spoutPtr == 0) {
            ensureInitialized();
            if (this.spoutPtr == 0) return 0;
            initReceiver(this.currentSpoutID, width, height);
        }

        if (this.dynTex != null) {
            this.receiveTexture(dynTex);
            return dynTex.getId();
        }
        return 0;
    }

    public void releaseSpout() {
        if (spoutPtr != 0) {
            cleanupReceiver();
            try {
                this.deInit(spoutPtr);
            } catch (Throwable ignored) {
            }
            spoutPtr = 0;
        }
        if (dynTex != null) {
            RenderSystem.bindTexture(0);
            dynTex.close();
            dynTex = null;
        }
    }
}