package top.yunmouren.craftbrowser.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;


public class MissingNCEFScreen extends Screen {

    public MissingNCEFScreen() {
        super(new TextComponent("Missing NCEF"));
    }

    @Override
    protected void init() {
        String text = "Open Download Page";
        TextComponent label = new TextComponent(text);

        int textWidth = this.font.width(label);

        int buttonWidth = textWidth + 20;
        int buttonHeight = 20;

        int x = (this.width - buttonWidth) / 2;
        int y = this.height / 2;

        this.addRenderableWidget(new Button(
                x, y, buttonWidth, buttonHeight,
                label,
                button -> Util.getPlatform().openUri("https://github.com/GaoXanSheng/CraftBrowser/releases")
        ));

    }
    private void drawCenteredText(PoseStack poseStack, String text, int y, int color) {
        this.font.drawShadow(poseStack, text,
                (this.width - this.font.width(text)) / 2.0f, // 自动算出居中位置
                y,
                color
        );
    }
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        drawCenteredText(poseStack, "NCEF is missing!", this.height / 2 - 40, 0xFF0000);
        drawCenteredText(poseStack, "Please download it from GitHub", this.height / 2 - 25, 0xFFFFFF);
        drawCenteredText(poseStack, "And placed in: " + Minecraft.getInstance().gameDirectory.toPath().toAbsolutePath(), this.height / 2 - 10, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }
}
