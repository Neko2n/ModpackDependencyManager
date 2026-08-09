package dev.nekotune.mdm.client.gui.config;

import dev.nekotune.mdm.Config;
import dev.nekotune.mdm.client.gui.config.widgets.SettingsListWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;

public enum ConfigScreenSetting implements SettingsListWidget.Setting {
    PRODUCTION(new InputElement.Toggle(Config.INSTANCE.production)),
    HIDE_FORCED(new InputElement.Toggle(Config.INSTANCE.hideForced)),
    WARN_ENABLED(new InputElement.Toggle(Config.INSTANCE.warnEnabled)),
    PROMPT_ENABLED(new InputElement.Toggle(Config.INSTANCE.promptEnabled)),
    DISABLE_COMPATIBILITY_WARNINGS(new InputElement.Toggle(Config.INSTANCE.disableCompatibilityWarnings));

    private static final String KEY = AbstractConfigScreen.Components.KEY + ".settings";

    public final InputElement<?> inputElement;
    private final String translationKey;

    private ConfigScreenSetting(final InputElement<?> inputElement) {
        this.inputElement = inputElement;
        this.translationKey = this.name().toLowerCase().replace('_', '-');
    }

    @Override
    public StringWidget createLabel(final Font font) {
        final String key = KEY + "." + translationKey;
        final var widget = new StringWidget(Component.translatable(key), font);
        final Tooltip tooltip = Tooltip.create(Component.translatable(key + ".tooltip"));
        widget.setTooltip(tooltip);
        return widget;
    }

    @Override
    public LayoutElement createInput(final Font font) {
        return inputElement.create(font);
    }

    private static abstract class InputElement<T> {

        protected T value;

        public InputElement(final T defaultValue) {
            value = defaultValue;
        }

        public abstract LayoutElement create(final Font font);

        /**
         * Simple input button representing a boolean setting.
         */
        public static final class Toggle extends InputElement<Boolean> {
            public Toggle(final boolean defaultValue) {
                super(defaultValue);
            }

            @Override
            public LayoutElement create(final Font font) {
                return Button.builder(getMessage(),
                        (final Button button) -> {
                            value = !value;
                            button.setMessage(getMessage());
                        }).width(60)
                        .build();
            }

            private Component getMessage() {
                return Component.literal(this.value ? "ON" : "OFF");
            }
        };
    }
}