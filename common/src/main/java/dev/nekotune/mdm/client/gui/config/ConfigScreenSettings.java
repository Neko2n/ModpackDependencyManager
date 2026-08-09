package dev.nekotune.mdm.client.gui.config;

import java.util.function.Consumer;

import dev.nekotune.mdm.Config;
import dev.nekotune.mdm.client.gui.config.widgets.SettingsListWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

public enum ConfigScreenSettings implements SettingsListWidget.Setting {
    PRODUCTION(new InputElement.Toggle(Config.INSTANCE.production,
            value -> Config.INSTANCE.production = value)),
    HIDE_FORCED(new InputElement.Toggle(Config.INSTANCE.hideForced,
            value -> Config.INSTANCE.hideForced = value)),
    WARN_ENABLED(new InputElement.Toggle(Config.INSTANCE.warnEnabled,
            value -> Config.INSTANCE.warnEnabled = value)),
    PROMPT_ENABLED(new InputElement.Toggle(Config.INSTANCE.promptEnabled,
            value -> Config.INSTANCE.promptEnabled = value)),
    DISABLE_COMPATIBILITY_WARNINGS(new InputElement.Toggle(Config.INSTANCE.disableCompatibilityWarnings,
            value -> Config.INSTANCE.disableCompatibilityWarnings = value)),
    BUTTON_OFFSET(new InputElement.InputInts(
            new int[]{Config.INSTANCE.buttonOffset.x, Config.INSTANCE.buttonOffset.y},
            value -> {
                Config.INSTANCE.buttonOffset.x = value[0];
                Config.INSTANCE.buttonOffset.y = value[1];
            }));

    private static final String KEY = AbstractConfigScreen.Components.KEY + ".settings";

    public final InputElement<?> inputElement;
    private final String translationKey;

    private ConfigScreenSettings(final InputElement<?> inputElement) {
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
        protected final Consumer<T> onValueChanged;

        public InputElement(final T defaultValue, final Consumer<T> onValueChanged) {
            this.value = defaultValue;
            this.onValueChanged = onValueChanged;
        }

        public abstract LayoutElement create(final Font font);

        /**
         * Simple input button representing a boolean setting.
         */
        public static final class Toggle extends InputElement<Boolean> {
            public Toggle(final boolean defaultValue, final Consumer<Boolean> onValueChanged) {
                super(defaultValue, onValueChanged);
            }

            @Override
            public LayoutElement create(final Font font) {
                return Button.builder(getMessage(),
                        (final Button button) -> {
                            value = !value;
                            onValueChanged.accept(value);
                            button.setMessage(getMessage());
                        }).size(60, 20)
                        .build();
            }

            private Component getMessage() {
                return Component.literal(this.value ? "ON" : "OFF");
            }
        }

        /**
         * Input which accepts N integers where N >= 1.
         */
        public static final class InputInts extends InputElement<int[]> {

            private final int size;

            public InputInts(final int[] defaultValue, final Consumer<int[]> onValueChanged) {
                super(defaultValue, onValueChanged);
                this.size = defaultValue.length;
            }

            @Override
            public LayoutElement create(final Font font) {
                final LinearLayout layout = LinearLayout.horizontal();
                for (int i = 0; i < this.size; i++) {
                    final var editBox = new EditBox(font, 40, 20, Component.empty());
                    editBox.setFilter(text -> (text.isEmpty() || text.matches("-?\\d*"))
                            && text.length() <= 4);
                    editBox.setValue(String.valueOf(this.value[i]));
                    final int i$immutable = i;
                    editBox.setResponder(text -> {
                        try {
                            this.value[i$immutable] = Integer.parseInt(text);
                            this.onValueChanged.accept(this.value);
                        } catch (final NumberFormatException e) {}
                    });
                    layout.addChild(editBox);
                }
                layout.arrangeElements();
                return layout;
            }
        }
    }
}