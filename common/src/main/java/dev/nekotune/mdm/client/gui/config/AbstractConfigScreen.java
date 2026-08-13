package dev.nekotune.mdm.client.gui.config;

import java.util.function.Supplier;

import dev.nekotune.mdm.Config;
import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.client.gui.config.widgets.ScrollListContent;
import dev.nekotune.mdm.client.gui.config.widgets.SettingsListWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class AbstractConfigScreen extends Screen {

    public static final String KEY = Constants.MOD_ID + ".screen.config";
    public static final Component EXIT_BUTTON = Component
            .translatableWithFallback(KEY + ".button.exit", "Save & Exit");
    public static final Component APPLY_BUTTON = Component
            .translatableWithFallback(KEY + ".button.apply", "Apply");
    protected static final int SCROLL_LIST_PADDING = 80;
    private static final int BAR_BG_COLOR = 0x65000000;

    protected final Supplier<Integer> barHeight;
    public final Screen lastScreen;
    public final Button backButton;
    public final SettingsListWidget scrollList;

    protected AbstractConfigScreen(final Component title, final Screen lastScreen) {
        super(title);
        this.lastScreen = lastScreen;
        this.barHeight = () -> this.height / 6;
        this.backButton = Button.builder(Component.empty(), $ -> this.onClose())
                .size(150, 20)
                .pos(this.width / 2 - 75, this.height - barHeight.get() / 2 - 10)
                .build();
        final int scrollListWidth = this.width - SCROLL_LIST_PADDING * 2;
        final var listBuilder = new ScrollListContent.Builder(scrollListWidth, this.font);
        buildScrollList(listBuilder);
        this.scrollList = new SettingsListWidget(SCROLL_LIST_PADDING, this.barHeight.get(),
                scrollListWidth, this.height - barHeight.get() * 2, listBuilder.build());
    }

    protected abstract void buildScrollList(final ScrollListContent.Builder builder);

    @Override
    protected void init() {
        super.init();
        this.addWidget(scrollList);
        this.addWidget(backButton);
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Draw scroll layout contents
        scrollList.render(guiGraphics, mouseX, mouseY, partialTick);

        // Draw bars
        final int barHeight = this.barHeight.get();
        guiGraphics.fill(0, 0, this.width, barHeight - 1, BAR_BG_COLOR);
        guiGraphics.fill(0, this.height, this.width, this.height - barHeight + 1, BAR_BG_COLOR);
        guiGraphics.hLine(0, this.width, barHeight - 1, 0x95000000);
        guiGraphics.hLine(0, this.width, barHeight, 0x55FFFFFF);
        guiGraphics.hLine(0, this.width, this.height - barHeight, 0x95000000);
        guiGraphics.hLine(0, this.width, this.height - barHeight - 1, 0x55FFFFFF);

        // Draw bar contents
        guiGraphics.drawCenteredString(this.font, this.title,
                this.width / 2, barHeight / 2, 0xFFFFFFFF);
        if (this.lastScreen instanceof AbstractConfigScreen) {
            backButton.setMessage(APPLY_BUTTON);
        } else {
            backButton.setMessage(EXIT_BUTTON);
        }
        backButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Config.INSTANCE.save();
        this.minecraft.setScreen(lastScreen);
    }
}
