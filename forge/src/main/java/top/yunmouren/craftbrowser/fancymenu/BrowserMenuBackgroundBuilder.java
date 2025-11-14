package top.yunmouren.craftbrowser.fancymenu;

import de.keksuccino.fancymenu.customization.background.MenuBackgroundBuilder;
import de.keksuccino.fancymenu.customization.background.SerializedMenuBackground;
import de.keksuccino.fancymenu.util.LocalizationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BrowserMenuBackgroundBuilder extends MenuBackgroundBuilder<BrowserMenuBackground> {
    public BrowserMenuBackgroundBuilder(String uniqueIdentifier) {
        super(uniqueIdentifier);
    }

    @Override
    public void buildNewOrEditInstance(@Nullable Screen screen, @Nullable BrowserMenuBackground browserMenuBackground, @NotNull Consumer<BrowserMenuBackground> consumer) {
        BrowserMenuBackground back = browserMenuBackground != null ? (BrowserMenuBackground) browserMenuBackground.copy() : new BrowserMenuBackground(this);

        BrowserBackgroundConfigScreen configScreen = new BrowserBackgroundConfigScreen(back, (url) -> {
            if (url != null) {
                back.loadUrl(url.url);
            }
            consumer.accept(back);
            Minecraft.getInstance().setScreen(screen);
        });

        net.minecraft.client.Minecraft.getInstance().setScreen(configScreen);
    }


    @Override
    public BrowserMenuBackground deserializeBackground(SerializedMenuBackground serializedMenuBackground) {
        var browserMenuBackground = new BrowserMenuBackground(this);
        String url = serializedMenuBackground.getValue("browser_menu_background_url");

        if (url != null) {
            browserMenuBackground.loadUrl(url);
        }
        return browserMenuBackground;
    }

    @Override
    public SerializedMenuBackground serializedBackground(BrowserMenuBackground browserMenuBackground) {
        SerializedMenuBackground serialized = new SerializedMenuBackground();
        if (browserMenuBackground.url != null) {
            serialized.putProperty("browser_menu_background_url", browserMenuBackground.url);
        }
        return serialized;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("craftbrowser");
    }

    @Override
    public @Nullable Component[] getDescription() {
        return LocalizationUtils.splitLocalizedLines("NCEF URL");
    }
}
