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
    private static final int MIN_SCALE = 16;
    private static final int MAX_SCALE = 256;
    private static final int SCALE_STEP = 16;
    private static final int SCALE_SLOTS = (MAX_SCALE - MIN_SCALE) / SCALE_STEP;

    private final BlockPos pos;
    private EditBox urlEditBox;

    private final String initialUrl;
    private final double initialVolume;
    private final int initialScale;
    private final int initialPermission;
    private double currentVolume;
    private int currentScale;
    private int currentPermission;
    private volatile String liveUrl = "";

    public BrowserUrlScreen(BlockPos pos, String url, double volume, int pixelsPerBlock, int editPermission) {
        super(Component.literal("Set Browser URL & Volume"));
        this.pos = pos;
        this.initialUrl = url;
        this.initialVolume = volume;
        this.initialScale = pixelsPerBlock;
        this.initialPermission = editPermission;
        this.currentVolume = volume;
        this.currentScale = pixelsPerBlock;
        this.currentPermission = editPermission;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.urlEditBox = new EditBox(this.font, centerX - 100, centerY - 70, 200, 20, Component.literal("URL"));
        this.urlEditBox.setMaxLength(32767);
        this.urlEditBox.setValue(initialUrl);
        this.addRenderableWidget(this.urlEditBox);
        this.setInitialFocus(this.urlEditBox);

        this.addRenderableWidget(new AbstractSliderButton(centerX - 100, centerY - 42, 200, 20,
                Component.literal(volumeLabel(this.currentVolume)), toSlider(this.currentVolume)) {

            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal(volumeLabel(this.value)));
            }

            @Override
            protected void applyValue() {
                BrowserUrlScreen.this.currentVolume = this.value;
            }
        });

        this.addRenderableWidget(new AbstractSliderButton(centerX - 100, centerY - 16, 200, 20,
                Component.literal(scaleLabel(this.currentScale)), toScaleSlider(this.currentScale)) {

            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal(scaleLabel(fromScaleSlider(this.value))));
            }

            @Override
            protected void applyValue() {
                BrowserUrlScreen.this.currentScale = fromScaleSlider(this.value);
            }
        });

        this.addRenderableWidget(Button.builder(Component.literal(permissionLabel(this.currentPermission)), button -> {
            this.currentPermission = this.currentPermission == 0 ? 1 : 0;
            button.setMessage(Component.literal(permissionLabel(this.currentPermission)));
        }).bounds(centerX - 100, centerY + 10, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Confirm"), button -> this.saveAndClose())
                .bounds(centerX - 100, centerY + 38, 98, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> this.onClose())
                .bounds(centerX + 2, centerY + 38, 98, 20).build());

        this.fetchLiveUrl();
    }

    private static String volumeLabel(double value) {
        return "Volume: " + (int) (value * 100) + "%";
    }

    private static String scaleLabel(int pixelsPerBlock) {
        return "Resolution: " + pixelsPerBlock + " px/block";
    }

    private static String permissionLabel(int permission) {
        return "Who can edit: " + (permission == 0 ? "Everyone" : "Owner only");
    }

    private static double toSlider(double volume) {
        return Math.max(0.0, Math.min(1.0, volume));
    }

    private static double toScaleSlider(int pixelsPerBlock) {
        int index = (pixelsPerBlock - MIN_SCALE) / SCALE_STEP;
        return Math.max(0, Math.min(SCALE_SLOTS, index)) / (double) SCALE_SLOTS;
    }

    private static int fromScaleSlider(double value) {
        int index = (int) Math.round(value * SCALE_SLOTS);
        return MIN_SCALE + index * SCALE_STEP;
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
            if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://") && !newUrl.startsWith("ncef://")) {
                newUrl = "https://" + newUrl;
            }
            if (!newUrl.equals(initialUrl) || this.currentVolume != initialVolume
                    || this.currentScale != initialScale || this.currentPermission != initialPermission) {
                BrowserBlockNetworkHandler.sendToServer(this.pos, newUrl, this.currentVolume, this.currentScale, this.currentPermission);
            }
        }
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 100, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Enter URL:", this.width / 2 - 100, this.height / 2 - 82, 0xA0A0A0, false);
        guiGraphics.drawCenteredString(this.font, "Current: " + this.liveUrl, this.width / 2, this.height / 2 + 64, 0x808080);
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
