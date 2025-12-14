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
    // 不需要单独的 Button 变量除非你需要禁用它，这里为了简洁省略了 confirmButton 的成员变量引用

    private final String initialUrl;
    private double currentVolume; // 当前音量变量

    public BrowserUrlScreen(BrowserMasterBlockEntity blockEntity) {
        super(Component.literal("Set Browser URL & Volume"));
        this.blockEntity = blockEntity;
        this.initialUrl = blockEntity.getUrl();
        // 假设 BlockEntity 有 getVolume()，如果没有请默认 1.0 或添加该方法
        // this.currentVolume = blockEntity.getVolume();
        // 暂时用 1.0 代替，如果你有 getVolume 请替换下面这就话：
        this.currentVolume = 1.0;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // URL 输入框
        this.urlEditBox = new EditBox(this.font, centerX - 100, centerY - 40, 200, 20, Component.literal("URL"));
        this.urlEditBox.setMaxLength(32767);
        this.urlEditBox.setValue(initialUrl);
        this.addRenderableWidget(this.urlEditBox);
        this.setInitialFocus(this.urlEditBox);

        // 音量滑块 (AbstractSliderButton)
        // 参数: x, y, width, height, message, initialValue
        this.addRenderableWidget(new AbstractSliderButton(centerX - 100, centerY - 10, 200, 20, Component.literal("Volume"), this.currentVolume) {
            @Override
            protected void updateMessage() {
                // 显示百分比，例如 "Volume: 50%"
                int percent = (int) (this.value * 100);
                this.setMessage(Component.literal("Volume: " + percent + "%"));
            }

            @Override
            protected void applyValue() {
                // 当滑块移动时更新 currentVolume
                BrowserUrlScreen.this.currentVolume = this.value;
            }
        });

        // 确认按钮
        this.addRenderableWidget(Button.builder(Component.literal("Confirm"), button -> {
            this.saveAndClose();
        }).bounds(centerX - 100, centerY + 20, 98, 20).build());

        // 取消按钮
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
            // 发送 URL 和 Volume 到服务器
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