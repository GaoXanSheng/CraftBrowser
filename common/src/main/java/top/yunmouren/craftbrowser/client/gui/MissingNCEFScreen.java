package top.yunmouren.craftbrowser.client.gui;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;


public class MissingNCEFScreen extends Screen {

    public MissingNCEFScreen() {
        super(Component.literal("Missing NCEF"));
    }

    @Override
    protected void init() {
        String text = "Open Download Page";
        Component label = Component.literal(text);
        int textWidth = this.font.width(label);

        int buttonWidth = textWidth + 20;
        int buttonHeight = 20;

        int x = (this.width - buttonWidth) / 2;
        int y = this.height / 2;

        this.addRenderableWidget(
                Button.builder(Component.literal("Open GitHub"), button -> {
                            Util.getPlatform().openUri("https://github.com/GaoXanSheng/CraftBrowser/releases");
                        })
                        .bounds(x, y, buttonWidth, buttonHeight)
                        .build()
        );

    }

    private void drawCenteredText(GuiGraphics guiGraphics, String text, int y, int color) {
        guiGraphics.drawCenteredString(this.font, text, this.width / 2, y, color);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        drawCenteredText(guiGraphics, "NCEF is missing!", this.height / 2 - 40, 0xFF0000);
        drawCenteredText(guiGraphics, "Please download it from GitHub", this.height / 2 - 25, 0xFFFFFF);
        drawCenteredText(guiGraphics, "And placed in: " + Minecraft.getInstance().gameDirectory.toPath().toAbsolutePath(), this.height / 2 - 10, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
