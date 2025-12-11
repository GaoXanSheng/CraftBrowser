package top.yunmouren.craftbrowser.client.browser.core;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import spout.JNISpout;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.client.config.Config;

public class BrowserRender extends JNISpout implements AutoCloseable {
    private final long spoutPtr = this.init();
    private final String currentSpoutID;

    public final int[] dim = new int[2];

    private DynamicTexture dynTex;

    public BrowserRender(String spoutID, int width, int height) {
        super();
        this.currentSpoutID = "WebViewSpoutCapture_"+spoutID;
        this.dim[0] = Math.max(1, width);
        this.dim[1] = Math.max(1, height);
        dynTex = new DynamicTexture(this.dim[0], this.dim[1], true);
        boolean connected = this.createReceiver(currentSpoutID, dim, spoutPtr);
        if (connected) {
            Craftbrowser.LOGGER.info("Spout Connected successfully to '{}'", currentSpoutID);
        }
    }

    public BrowserRender() {
        this(Config.CLIENT.customizeSpoutID.get(),
                Minecraft.getInstance().getWindow().getScreenWidth(),
                Minecraft.getInstance().getWindow().getScreenHeight());
    }

    public void receiveTexture(DynamicTexture dynamicTexture) {
        this.receiveTexture(dim, dynamicTexture.getId(), GL11.GL_TEXTURE_2D, false, spoutPtr);
    }

    public int render(int width, int height) {
        if (width <= 0 || height <= 0) return 0;
        if (this.dynTex != null) {
            this.receiveTexture(dynTex);
            return dynTex.getId();
        }
        return 0;
    }

    @Override
    public void close() {
        RenderSystem.bindTexture(0);
        this.releaseReceiver(spoutPtr);
        this.deInit(spoutPtr);
        dynTex.close();
    }
}