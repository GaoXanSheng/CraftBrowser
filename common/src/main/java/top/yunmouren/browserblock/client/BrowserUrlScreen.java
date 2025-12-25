package top.yunmouren.browserblock.client;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import top.yunmouren.browserblock.block.BrowserMasterBlockEntity;
import top.yunmouren.craftbrowser.server.network.packet.SetBrowserUrlPacket;

public class BrowserUrlScreen extends Screen {
    private final BrowserMasterBlockEntity blockEntity;
    private EditBox urlEditBox;

    private final String initialUrl;
    private double currentVolume; // 当前音量变量

    public BrowserUrlScreen(BrowserMasterBlockEntity blockEntity) {
        super(Component.literal("Set Browser URL & Volume"));
        this.blockEntity = blockEntity;
        this.initialUrl = blockEntity.getUrl();
        this.currentVolume = 1.0;
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

        this.addRenderableWidget(new AbstractSliderButton(centerX - 100, centerY - 10, 200, 20, Component.literal("Volume"), this.currentVolume) {
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

        this.addRenderableWidget(Button.builder(Component.literal("Confirm"), button -> this.saveAndClose()).bounds(centerX - 100, centerY + 20, 98, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> this.onClose()).bounds(centerX + 2, centerY + 20, 98, 20).build());
    }

    private void saveAndClose() {
        String newUrl = this.urlEditBox.getValue();
        if (!newUrl.isEmpty()) {
            if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                newUrl = "https://" + newUrl;
            }
            NetworkManager.sendToServer(new SetBrowserUrlPacket(blockEntity.getBlockPos(), newUrl, this.currentVolume));
        }
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBlurredBackground(1);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 70, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Enter URL:", this.width / 2 - 100, this.height / 2 - 52, 0xA0A0A0, false);
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