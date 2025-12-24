package top.yunmouren.craftbrowser.client.browser.core;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import spout.JNISpout;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.client.config.Config;

import java.util.Locale;

public class BrowserRender extends JNISpout implements AutoCloseable {

    private long spoutPtr = 0;
    private final String currentSpoutID;
    public final int[] dim = new int[2];

    private DynamicTexture dynTex;
    private ResourceLocation textureLocation;
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

    public ResourceLocation render(int width, int height) {
        if (width <= 0 || height <= 0) return null;
        if (!checkInit()) return null;

        if (!isConnected) {
            boolean created = this.createReceiver(currentSpoutID, dim, spoutPtr);
            if (created) {
                String senderName = this.getSenderName(spoutPtr);
                if (senderName != null && senderName.equals(this.currentSpoutID)) {
                    isConnected = true;
                } else {
                    this.setReceiverName(this.currentSpoutID, spoutPtr);
                }
            } else {
                return null;
            }
        }
        if (dynTex == null || dynTex.getPixels().getWidth() != width || dynTex.getPixels().getHeight() != height) {
            if (dynTex != null) {
                dynTex.close();
            }
            dynTex = new DynamicTexture(width, height, true);
            textureLocation = Minecraft.getInstance()
                    .getTextureManager()
                    .register("browser/" + sanitizePath(currentSpoutID), dynTex);
        }

        boolean success = this.receiveTexture(
                dim,
                dynTex.getId(),
                GL11.GL_TEXTURE_2D,
                false,
                spoutPtr
        );

        if (success) {
            return textureLocation;
        }
        return null;
    }
    private String sanitizePath(String input) {
        return input
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9/._-]", "_");
    }
    @Override
    public void close() {
        if (spoutPtr != 0) {
            RenderSystem.recordRenderCall(() -> {
                if (dynTex != null) {
                    dynTex.close();
                    dynTex = null;
                    textureLocation = null;
                }
                this.releaseReceiver(spoutPtr);
                this.deInit(spoutPtr);
                spoutPtr = 0;
                isConnected = false;
            });
        }
    }
}
