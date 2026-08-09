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
    protected SettingsListWidget.ListContent buildScrollList(SettingsListWidget.ListContent.Builder builder) {
        for (final var setting : ConfigScreenSettings.values()) {
           builder = builder.addSetting(setting);
        }
        return builder.build();
    }
}
