package top.yunmouren.browserblock.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import top.yunmouren.browserblock.block.BrowserMasterBlockEntity;
import top.yunmouren.browserblock.network.BrowserBlockNetworkHandler;
import top.yunmouren.craftbrowser.client.browser.Controller.IBrowserController;

import java.util.concurrent.CompletableFuture;

public class BrowserUrlScreen extends Screen {
    private final BlockPos pos;
    private EditBox urlEditBox;

    private final String initialUrl;
    private final double initialVolume;
    private double currentVolume;
    private volatile String liveUrl = "";

    public BrowserUrlScreen(BlockPos pos, String url, double volume) {
        super(Component.literal("Set Browser URL & Volume"));
        this.pos = pos;
        this.initialUrl = url;
        this.initialVolume = volume;
        this.currentVolume = volume;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        this.urlEditBox = new EditBox(this.font, centerX - 100, centerY - 40, 200, 20, Component.literal("URL"));
        this.urlEditBox.setMaxLength(32767);
        this.urlEditBox.setValue(initialUrl);
        this.addRenderableWidget(this.urlEditBox);
        this.setInitialFocus(this.urlEditBox);
        this.addRenderableWidget(new AbstractSliderButton(centerX - 100, centerY - 10, 200, 20, Component.literal("Volume: " + (int) (this.currentVolume * 100) + "%"), this.currentVolume) {

            @Override
            protected void updateMessage() {
                int percent = (int) (this.value * 100);
                this.setMessage(Component.literal("Volume: " + percent + "%"));
            }

            @Override
            protected void applyValue() {
                BrowserUrlScreen.this.currentVolume = this.value;
            }
        });
        this.addRenderableWidget(Button.builder(Component.literal("Confirm"), button ->
                {
                    this.saveAndClose();
                }).
                bounds(centerX - 100, centerY + 20, 98, 20).
                build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button ->
                {
                    this.onClose();
                }).
                bounds(centerX + 2, centerY + 20, 98, 20).
                build());
        this.fetchLiveUrl();
    }

    private void fetchLiveUrl() {
        if (this.minecraft == null || this.minecraft.level == null) return;
        if (!(this.minecraft.level.getBlockEntity(this.pos) instanceof BrowserMasterBlockEntity be)) return;

        IBrowserController controller = be.getBrowserController();
        if (controller == null) {
            this.liveUrl = "(browser not running)";
            return;
        }

        CompletableFuture.runAsync(() -> {
            String url;
            try {
                url = controller.GetUrl();
            } catch (Throwable t) {
                url = "(unavailable: " + t.getMessage() + ")";
            }
            final String resolved = url == null || url.isEmpty() ? "(empty)" : url;
            Minecraft.getInstance().execute(() -> this.liveUrl = resolved);
        });
    }

    private void saveAndClose() {
        String newUrl = this.urlEditBox.getValue();
        if (!newUrl.isEmpty()) {
            if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                newUrl = "https://" + newUrl;
            }
            if (!newUrl.equals(initialUrl) || this.currentVolume != initialVolume) {
                BrowserBlockNetworkHandler.sendToServer(this.pos, newUrl, this.currentVolume);
            }
        }
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 70, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Enter URL:", this.width / 2 - 100, this.height / 2 - 52, 0xA0A0A0, false);
        guiGraphics.drawCenteredString(this.font, "Current: " + this.liveUrl, this.width / 2, this.height / 2 + 46, 0x808080);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.saveAndClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
