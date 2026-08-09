package dev.nekotune.mdm.client.gui.config;

import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.client.gui.config.widgets.SettingsListWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MainConfigScreen extends AbstractConfigScreen {

    protected static final String KEY = AbstractConfigScreen.KEY + ".main";
    protected static final Component TITLE = Component
                .translatableWithFallback(KEY + ".title", Constants.MOD_NAME)
                .withStyle(ChatFormatting.BOLD);

    public MainConfigScreen(final Screen lastScreen) {
        super(TITLE, lastScreen);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected SettingsListWidget buildScrollList() {
        final int scrollListWidth = this.width - SCROLL_LIST_PADDING * 2;
        var listBuilder = new SettingsListWidget.ListContent.Builder(scrollListWidth, this.font);
        for (final var setting : ConfigScreenSettings.values()) {
            listBuilder = listBuilder.addSetting(setting);
        }
        return new SettingsListWidget(SCROLL_LIST_PADDING, this.barHeight.get(),
                scrollListWidth, this.height - barHeight.get() * 2, listBuilder.build());
    }
}
