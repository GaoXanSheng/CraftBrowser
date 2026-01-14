package top.yunmouren.browserblock.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import top.yunmouren.browserblock.block.BrowserMasterBlockEntity;
import top.yunmouren.browserblock.network.BrowserBlockNetworkHandler;

public class BrowserUrlScreen extends Screen {
    private final BrowserMasterBlockEntity blockEntity;
    private EditBox urlEditBox;

    private final String initialUrl;
    private double currentVolume;

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
                // 显示百分比，例如 "Volume: 50%"
                int percent = (int) (this.value * 100);
                this.setMessage(Component.literal("Volume: " + percent + "%"));
            }

            @Override
            protected void applyValue() {
                BrowserUrlScreen.this.currentVolume = this.value;
            }
        });
        this.addRenderableWidget(Button.builder(Component.literal("Confirm"), button -> {
            this.saveAndClose();
        }).bounds(centerX - 100, centerY + 20, 98, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> {
            this.onClose();
        }).bounds(centerX + 2, centerY + 20, 98, 20).build());
    }

    private void saveAndClose() {
        String newUrl = this.urlEditBox.getValue();
        if (!newUrl.isEmpty()) {
            if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                newUrl = "https://" + newUrl;
            }
            BrowserBlockNetworkHandler.sendToServer(blockEntity.getBlockPos(), newUrl, this.currentVolume);
        }
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 70, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Enter URL:", this.width / 2 - 100, this.height / 2 - 52, 0xA0A0A0, false);
        // 滑块上方可以加个标签，但滑块本身已经显示文字了，所以这里不需要额外绘制
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