package dev.nekotune.mdm.client.gui.config;

import java.util.Optional;
import java.util.function.Supplier;

import dev.nekotune.mdm.Config;
import dev.nekotune.mdm.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.Component;

public abstract class AbstractConfigScreen extends Screen {

    public static interface Components {
        public static final String KEY = Constants.MOD_ID + ".screen.config";
        public static final Component EXIT_BUTTON = Component
                .translatableWithFallback(KEY + ".button.exit", "Save & Exit");
    }

    public final Screen lastScreen;

    public Optional<ScreenObjects> screenObjects = Optional.empty();

    protected static final class ScreenObjects {
        private static final int BAR_BG_COLOR = 0x65000000;
        
        public final AbstractConfigScreen screen;
        private final Supplier<Integer> barHeight;
        public final Button exitButton;
        public final Button backButton;

        public ScreenObjects(final AbstractConfigScreen screen) {
            this.screen = screen;
            this.barHeight = () -> screen.height / 6;
            this.backButton = new PageButton(8, 8, false, $ -> screen.onClose(), false);
            this.exitButton = Button.builder(Components.EXIT_BUTTON, $ -> screen.exit())
                    .size(150, 20)
                    .pos(screen.width / 2 - 75, screen.height - barHeight.get() / 2 - 10)
                    .build();
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
            exitButton.render(guiGraphics, mouseX, mouseY, partialTick);
            if (screen.shouldCloseOnEsc()) {
                backButton.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }

    protected AbstractConfigScreen(final Component title, final Screen lastScreen) {
        super(title);
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();
        final ScreenObjects screenObjects = new ScreenObjects(this);
        this.screenObjects = Optional.of(screenObjects);
        this.addWidget(screenObjects.exitButton);
        if (this.shouldCloseOnEsc()) {
            this.addWidget(screenObjects.backButton);
        }
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.screenObjects.ifPresent(v -> v.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }

    @Override
    public void removed() {
        if (!(this.minecraft.screen instanceof AbstractConfigScreen)) {
            Config.INSTANCE.save();
        }
    }

    public void exit() {
        if (this.lastScreen instanceof final AbstractConfigScreen configScreen) {
            configScreen.exit();
        } else {
            this.onClose();
        }
    }
}
