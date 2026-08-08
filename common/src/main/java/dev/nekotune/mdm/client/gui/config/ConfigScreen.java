package dev.nekotune.mdm.client.gui.config;

import java.util.Optional;
import java.util.function.Supplier;

import dev.nekotune.mdm.Config;
import dev.nekotune.mdm.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {

    public static interface Components {
        public static final String KEY = Constants.MOD_ID + ".screen.config";
        public static final Component TITLE = Component
                .translatableWithFallback(KEY + ".title", Constants.MOD_NAME)
                .withStyle(ChatFormatting.BOLD);
        public static final Component CLOSE_BUTTON = Component
                .translatableWithFallback(KEY + ".button.close", "Save & Exit");
    }

    public final Screen lastScreen;

    public Optional<ScreenObjects> screenObjects = Optional.empty();

    private static final class ScreenObjects {
        private static final int BAR_BG_COLOR = 0x65000000;

        public final ConfigScreen screen;
        public final Button closeButton;
        private final Supplier<Integer> barHeight;

        public ScreenObjects(final ConfigScreen screen, final Button closeButton) {
            this.screen = screen;
            this.closeButton = closeButton;
            this.barHeight = () -> screen.height / 6;
        }

        public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
                final float partialTick) {
            // Draw scroll layout contents

            // Draw bars
            final int barHeight = this.barHeight.get();
            guiGraphics.fill(0, 0, screen.width, barHeight, BAR_BG_COLOR);
            guiGraphics.fill(0, screen.height, screen.width, screen.height - barHeight, BAR_BG_COLOR);

            // Draw bar contents
            guiGraphics.drawCenteredString(screen.font, screen.title,
                    screen.width / 2, barHeight / 2, 0xFFFFFFFF);
            this.closeButton.setPosition(screen.width / 2 - this.closeButton.getWidth() / 2,
                    screen.height - barHeight / 2 - this.closeButton.getHeight() / 2);
        }
    }

    public ConfigScreen(final Screen lastScreen) {
        super(Components.TITLE);
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();
        final ScreenObjects screenObjects = new ScreenObjects(this,
                Button.builder(Components.CLOSE_BUTTON, this::onClose).build());
        this.screenObjects = Optional.of(screenObjects);
        this.addRenderableWidget(screenObjects.closeButton);
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.screenObjects.ifPresent(v -> v.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public void onClose() {
        Config.INSTANCE.save();
        this.minecraft.setScreen(lastScreen);
    }

    private void onClose(Button button) {
        this.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
