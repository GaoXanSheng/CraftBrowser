package top.yunmouren.craftbrowser.client.browser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import spout.JNISpout;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.config.Config;


public class BrowserRender extends JNISpout {
    private long spoutPtr = 0;
    private boolean receiverConnected = false;
    private final Minecraft mc = Minecraft.getInstance();
    public int[] dim = new int[]{mc.getWindow().getScreenWidth(), mc.getWindow().getScreenHeight()};
    private DynamicTexture dynTex;
    public BrowserRender(){
        super();
        this.spoutPtr = this.init();
        if (Config.CLIENT.customizeSpoutIDEnabled.get()){
            receiverConnected = this.createReceiver(Config.CLIENT.customizeSpoutID.get(), dim, spoutPtr);
        }else {
            receiverConnected = this.createReceiver("WebViewSpoutCapture_" + BrowserProcess.getBrowserPort(), dim, spoutPtr);
        }
        if (!receiverConnected) {
            Craftbrowser.LOGGER.error("Failed to create Spout receiver! Is the sender running?");
        }
        if (dynTex == null) {
            dynTex = new DynamicTexture(dim[0], dim[1], true);
        }
    }
    public void receiveTexture(DynamicTexture dynamicTexture){
        var received = this.receiveTexture(dim, dynamicTexture.getId(), GL11.GL_TEXTURE_2D, false, spoutPtr);
        if (!received) {
            Craftbrowser.LOGGER.error("Spout receiveTexture failed. Releasing receiver.");
            releaseSpout();
        }
    }
    public int render(int width, int height){
        if (this.dynTex == null) {
            return 0;
        }
        if (dynTex.getPixels() != null && (dynTex.getPixels().getWidth() != width || dynTex.getPixels().getHeight() != height)) {
            dynTex.close();
            dynTex = new DynamicTexture(width,height, true);
        }
        this.receiveTexture(dynTex);

        return dynTex.getId();
    }
    private void releaseSpout() {
        if (receiverConnected) {
            this.releaseReceiver(spoutPtr);
            receiverConnected = false;
        }
        if (spoutPtr != 0) {
            this.deInit(spoutPtr);
            spoutPtr = 0;
        }
    }

}
