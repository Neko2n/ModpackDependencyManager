package dev.nekotune.mdm.client.gui.config;

import java.util.function.Function;

import dev.nekotune.mdm.Config;
import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.client.gui.config.dependencies.DependenciesScreen;
import dev.nekotune.mdm.client.gui.config.widgets.ScrollListContent;
import dev.nekotune.mdm.client.gui.config.widgets.input.ToggleInput;
import dev.nekotune.mdm.client.gui.config.widgets.input.VectorInput;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;

// TODO Fix settings being offset to the right, getting cut off by the scroll bar
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
    protected void populateSettings(final ScrollListContent.Builder builder) {
        builder.addButton(KEY + ".button.client-resources", (final Button button) -> {
            this.minecraft.setScreen(new DependenciesScreen(this, PackType.CLIENT_RESOURCES));
        });
        builder.addButton(KEY + ".button.server-data", (final Button button) -> {
            this.minecraft.setScreen(new DependenciesScreen(this, PackType.SERVER_DATA));
        });
        MainSettings.PRODUCTION.appendTo(builder, this.font);
        MainSettings.HIDE_FORCED.appendTo(builder, this.font);
        MainSettings.WARN_ENABLED.appendTo(builder, this.font);
        MainSettings.PROMPT_ENABLED.appendTo(builder, this.font);
        MainSettings.DISABLE_COMPATIBILITY_WARNINGS.appendTo(builder, this.font);
        MainSettings.BUTTON_OFFSET.appendTo(builder, this.font);
    }

    public static enum MainSettings {
        PRODUCTION(font -> new ToggleInput.TextToggle(Config.INSTANCE.production,
                value -> Config.INSTANCE.production = value)),
        HIDE_FORCED(font -> new ToggleInput.TextToggle(Config.INSTANCE.hideForced,
                value -> Config.INSTANCE.hideForced = value)),
        WARN_ENABLED(font -> new ToggleInput.TextToggle(Config.INSTANCE.warnEnabled,
                value -> Config.INSTANCE.warnEnabled = value)),
        PROMPT_ENABLED(font -> new ToggleInput.TextToggle(Config.INSTANCE.promptEnabled,
                value -> Config.INSTANCE.promptEnabled = value)),
        DISABLE_COMPATIBILITY_WARNINGS(font -> new ToggleInput.TextToggle(Config.INSTANCE.disableCompatibilityWarnings,
                value -> Config.INSTANCE.disableCompatibilityWarnings = value)),
        BUTTON_OFFSET(font -> new VectorInput(font,
                new int[] { Config.INSTANCE.buttonOffset.x, Config.INSTANCE.buttonOffset.y },
                value -> {
                    Config.INSTANCE.buttonOffset.x = value[0];
                    Config.INSTANCE.buttonOffset.y = value[1];
                }));
        
        public final Function<Font, LayoutElement> inputElement;
        public final String translationKey = KEY + "."
                + this.name().toLowerCase().replace('_', '-');
        
        private MainSettings(final Function<Font, LayoutElement> inputElement) {
            this.inputElement = inputElement;
        }

        public ScrollListContent.Builder appendTo(
                final ScrollListContent.Builder builder, final Font font) {
            return builder.addSetting(this.translationKey, this.inputElement.apply(font));
        }
    }
}
