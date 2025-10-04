package top.yunmouren.craftbrowser.fancymenu;

import de.keksuccino.fancymenu.util.file.type.types.TextFileType;
import de.keksuccino.fancymenu.util.rendering.ui.screen.CellScreen;
import de.keksuccino.fancymenu.util.rendering.ui.screen.resource.ResourceChooserScreen;
import de.keksuccino.fancymenu.util.rendering.ui.tooltip.Tooltip;
import de.keksuccino.fancymenu.util.rendering.ui.widget.button.ExtendedButton;
import de.keksuccino.fancymenu.util.resource.resources.text.IText;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class BrowserBackgroundConfigScreen extends CellScreen {
    protected Consumer<BrowserMenuBackground> callback;
    protected BrowserMenuBackground background;
    protected BrowserBackgroundConfigScreen(@NotNull BrowserMenuBackground background, @NotNull Consumer<BrowserMenuBackground> callback) {
        super(Component.literal("craftbrowser url"));
        this.background = background;
        this.callback = callback;
    }
    protected BrowserBackgroundConfigScreen(@NotNull Component title) {
        super(title);
    }
    @Override
    protected void init() {
        super.init();
        this.doneButton.setTooltipSupplier((consumes) -> {
            if (this.background.url == null || this.background.url.isEmpty()) {
                return Tooltip.of(Component.literal("URL Cannot Be empty"));
            }
            return null;
        });

    }
    protected void initCells() {
        this.addStartEndSpacerCell();
        this.addWidgetCell(new ExtendedButton(0, 0, 20, 20, Component.literal("WebUrl"), (button) -> {
            // 创建 ResourceChooserScreen 并保存到变量
            ResourceChooserScreen<IText, TextFileType> s = ResourceChooserScreen.text(null, (source) -> {
                if (source != null) {
                    background.url = source; // 保存到 background
                }
                // 回到当前配置界面
                Minecraft.getInstance().setScreen(this);
            });
            // 打开 ResourceChooserScreen
            Minecraft.getInstance().setScreen(s);
        }), true);
    }

    @Override
    protected void onCancel() {
        callback.accept(null);
    }

    @Override
    protected void onDone() {
        callback.accept(background);
    }
}
