package top.yunmouren.craftbrowser.client.browser.core;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import spout.JNISpout;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.client.config.Config;

public class BrowserRender extends JNISpout implements AutoCloseable {
    private long spoutPtr = 0;
    private final String currentSpoutID;
    public final int[] dim = new int[2];

    private DynamicTexture dynTex;
    private boolean isConnected = false;

    public BrowserRender(String spoutID) {
        super();
        this.currentSpoutID = "WebViewSpoutCapture_" + spoutID;
    }

    public BrowserRender() {
        this(Config.CLIENT.customizeSpoutID.get());
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

    public int render(int width, int height) {
        if (width <= 0 || height <= 0) return 0;
        if (!checkInit()) return 0;
        if (!isConnected) {
            boolean created = this.createReceiver(currentSpoutID, dim, spoutPtr);
            if (created) {
                String senderName = this.getSenderName(spoutPtr);
                if (senderName != null && senderName.equals(this.currentSpoutID)) {
                    Craftbrowser.LOGGER.info("Spout Connected successfully to '{}'", currentSpoutID);
                    isConnected = true;
                } else {
                    this.setReceiverName(this.currentSpoutID, spoutPtr);
                }
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
        boolean success = this.receiveTexture(dim, dynTex.getId(), GL11.GL_TEXTURE_2D, false, spoutPtr);
        if (success) {
            return dynTex.getId();
        } else {
            return 0;
        }
    }

    @Override
    public void close() {
        if (spoutPtr != 0) {
            RenderSystem.recordRenderCall(() -> {
                if (dynTex != null) {
                    RenderSystem.bindTexture(0);
                    dynTex.close();
                    dynTex = null;
                }
                this.releaseReceiver(spoutPtr);
                this.deInit(spoutPtr);
                spoutPtr = 0;
                isConnected = false;
            });
        }
    }
}