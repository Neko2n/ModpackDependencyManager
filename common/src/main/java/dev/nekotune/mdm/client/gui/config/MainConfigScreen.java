package dev.nekotune.mdm.client.gui.config;

import java.util.function.Consumer;
import java.util.function.Function;

import dev.nekotune.mdm.Config;
import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.client.gui.config.widgets.SettingsListWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
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
        builder = builder.addButton(KEY + ".button.resource-packs", (final Button button) -> {

        });
        builder = builder.addButton(KEY + ".button.data-packs", (final Button button) -> {

        });
        builder = MainSettings.PRODUCTION.appendTo(builder, font);
        builder = MainSettings.HIDE_FORCED.appendTo(builder, font);
        builder = MainSettings.WARN_ENABLED.appendTo(builder, font);
        builder = MainSettings.PROMPT_ENABLED.appendTo(builder, font);
        builder = MainSettings.DISABLE_COMPATIBILITY_WARNINGS.appendTo(builder, font);
        builder = MainSettings.BUTTON_OFFSET.appendTo(builder, font);
        return builder.build();
    }

    public static enum MainSettings {
        PRODUCTION(new ToggleSetting(Config.INSTANCE.production,
                value -> Config.INSTANCE.production = value)),
        HIDE_FORCED(new ToggleSetting(Config.INSTANCE.hideForced,
                value -> Config.INSTANCE.hideForced = value)),
        WARN_ENABLED(new ToggleSetting(Config.INSTANCE.warnEnabled,
                value -> Config.INSTANCE.warnEnabled = value)),
        PROMPT_ENABLED(new ToggleSetting(Config.INSTANCE.promptEnabled,
                value -> Config.INSTANCE.promptEnabled = value)),
        DISABLE_COMPATIBILITY_WARNINGS(new ToggleSetting(Config.INSTANCE.disableCompatibilityWarnings,
                value -> Config.INSTANCE.disableCompatibilityWarnings = value)),
        BUTTON_OFFSET(new VectorSetting(
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

        public SettingsListWidget.ListContent.Builder appendTo(
                final SettingsListWidget.ListContent.Builder builder, final Font font) {
            return builder.addSetting(this.translationKey, this.inputElement.apply(font));
        }

        /**
         * Simple input button representing a boolean setting.
         */
        public static final class ToggleSetting implements Function<Font, LayoutElement> {
            private boolean value;
            private final Consumer<Boolean> onValueChanged;

            public ToggleSetting(final boolean defaultValue, final Consumer<Boolean> onValueChanged) {
                this.value = defaultValue;
                this.onValueChanged = onValueChanged;
            }

            @Override
            public LayoutElement apply(final Font font) {
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
        public static final class VectorSetting implements Function<Font, LayoutElement> {
            private int[] value;
            private final Consumer<int[]> onValueChanged;
            private final int size;

            public VectorSetting(final int[] defaultValue, final Consumer<int[]> onValueChanged) {
                this.value = defaultValue;
                this.onValueChanged = onValueChanged;
                this.size = defaultValue.length;
            }

            @Override
            public LayoutElement apply(final Font font) {
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
                        } catch (final NumberFormatException e) {
                        }
                    });
                    layout.addChild(editBox);
                }
                layout.arrangeElements();
                return layout;
            }
        }
    }
}
