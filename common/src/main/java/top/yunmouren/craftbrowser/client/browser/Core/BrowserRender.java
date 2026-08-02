package top.yunmouren.craftbrowser.client.browser.Core;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import spout.JNISpout;
import top.yunmouren.craftbrowser.Craftbrowser;

public class BrowserRender extends JNISpout implements AutoCloseable {
    private long spoutPtr = 0;
    private final String currentSpoutID;
    public final int[] dim = new int[2];

    private DynamicTexture dynTex;
    private boolean isConnected = false;

    private float validU = 1.0f;
    private float validV = 1.0f;

    public BrowserRender(String spoutID) {
        super();
        this.currentSpoutID = spoutID;
        this.isClosed = false;
    }

    private boolean checkInit() {
        if (spoutPtr == 0) {
            try {
                spoutPtr = this.init();
            } catch (Throwable t) {
                Craftbrowser.LOGGER.error("Failed to init Spout native library", t);
                return false;
            }
        }
        return spoutPtr != 0;
    }

    /**
     * @param reqWidth
     * @param reqHeight
     */
    public synchronized int render(int reqWidth, int reqHeight) {
        if (isClosed || reqWidth <= 0 || reqHeight <= 0) return 0;
        if (!checkInit()) return 0;

        if (!isConnected) {
            this.setReceiverName(this.currentSpoutID, spoutPtr);
            boolean created = this.createReceiver(currentSpoutID, dim, spoutPtr);
            if (created) {
                isConnected = true;
                Craftbrowser.LOGGER.info("Spout Connected successfully to '{}'", currentSpoutID);
            } else {
                return 0;
            }
        }

        int senderW = this.getSenderWidth(spoutPtr);
        int senderH = this.getSenderHeight(spoutPtr);
        if (senderW <= 0 || senderH <= 0) return 0;

        if (dynTex == null || dynTex.getPixels().getWidth() != senderW || dynTex.getPixels().getHeight() != senderH) {
            if (dynTex != null) {
                dynTex.close();
            }
            dynTex = new DynamicTexture(senderW, senderH, true);
        }

        int glTexId = dynTex.getId();
        if (glTexId <= 0) return 0;

        boolean success = this.receiveTexture(dim, glTexId, GL11.GL_TEXTURE_2D, false, spoutPtr);
        if (success) {
            this.validU = (float) reqWidth / senderW;
            this.validV = (float) reqHeight / senderH;
            if (this.validU > 1.0f) this.validU = 1.0f;
            if (this.validV > 1.0f) this.validV = 1.0f;
            return glTexId;
        } else {
            return 0;
        }
    }
    public float getValidU() {
        return validU;
    }

    public float getValidV() {
        return validV;
    }

    private boolean isClosed = false;

    @Override
    public synchronized void close() {
        if (isClosed) return;
        isClosed = true;

        long ptrToFree = spoutPtr;
        spoutPtr = 0;
        isConnected = false;

        try {
            if (dynTex != null) {
                RenderSystem.bindTexture(0);
                dynTex.close();
                dynTex = null;
            }
        } catch (Throwable ignored) {}

        if (ptrToFree != 0) {
            try {
                this.releaseReceiver(ptrToFree);
            } catch (Throwable ignored) {}

            try {
                this.deInit(ptrToFree);
            } catch (Throwable ignored) {}
        }
    }
}