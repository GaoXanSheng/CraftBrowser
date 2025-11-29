package top.yunmouren.browserblock.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import top.yunmouren.browserblock.block.BrowserBlockEntity;
import top.yunmouren.browserblock.network.BrowserBlockNetworkHandler;

public class BrowserUrlScreen extends Screen {
    private final BrowserBlockEntity blockEntity;
    private EditBox urlEditBox;
    private Button confirmButton;
    private final String initialUrl;

    public BrowserUrlScreen(BrowserBlockEntity blockEntity) {
        super(Component.literal("设置浏览器 URL"));
        this.blockEntity = blockEntity;
        // 获取当前 URL 显示在输入框里
        this.initialUrl = blockEntity.getUrl();
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 1. 创建输入框
        // 参数：字体, x, y, 宽, 高, 标题
        this.urlEditBox = new EditBox(this.font, centerX - 100, centerY - 20, 200, 20, Component.literal("URL"));
        this.urlEditBox.setMaxLength(32767); // 允许长 URL
        this.urlEditBox.setValue(initialUrl); // 设置初始值
        this.addRenderableWidget(this.urlEditBox);

        // 设置焦点到输入框，方便直接打字
        this.setInitialFocus(this.urlEditBox);

        // 2. 创建“确定”按钮
        this.confirmButton = this.addRenderableWidget(Button.builder(Component.literal("确定"), button -> {
            this.saveAndClose();
        }).bounds(centerX - 100, centerY + 10, 98, 20).build());

        // 3. 创建“取消”按钮
        this.addRenderableWidget(Button.builder(Component.literal("取消"), button -> {
            this.onClose();
        }).bounds(centerX + 2, centerY + 10, 98, 20).build());
    }

    private void saveAndClose() {
        String newUrl = this.urlEditBox.getValue();
        if (!newUrl.isEmpty()) {
            // 简单补全 http
            if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                newUrl = "https://" + newUrl;
            }
            BrowserBlockNetworkHandler.sendToServer(blockEntity.getBlockPos(), newUrl);
        }
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        // 使用 guiGraphics 绘图
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        guiGraphics.drawString(this.font, "输入网址:", this.width / 2 - 100, this.height / 2 - 32, 0xA0A0A0, false);

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