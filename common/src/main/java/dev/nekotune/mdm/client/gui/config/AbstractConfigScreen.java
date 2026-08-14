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

    private final SettingsListWidget scrollList;
    protected final Supplier<Integer> barHeight;
    protected final Supplier<Integer> scrollListWidth;
    public final Screen lastScreen;
    public final Button backButton;

    protected AbstractConfigScreen(final Component title, final Screen lastScreen) {
        super(title);
        this.lastScreen = lastScreen;
        this.barHeight = () -> this.height / 6;
        this.scrollListWidth = () -> this.width - SCROLL_LIST_PADDING * 2;
        this.backButton = Button.builder(Component.empty(), $ -> this.onClose())
                .size(150, 20)
                .build();
        this.scrollList = new SettingsListWidget(0, 0, 0, 0);
    }

    protected abstract void buildScrollList(final ScrollListContent.Builder builder);

    protected final void rebuildScrollList() {
        this.scrollList.setPosition(SCROLL_LIST_PADDING, this.barHeight.get());
        final var listBuilder = new ScrollListContent.Builder(this.scrollListWidth.get(), this.font);
        buildScrollList(listBuilder);
        this.scrollList.setContent(listBuilder.build());
    }

    @Override
    protected void init() {
        super.init();
        this.rebuildScrollList();
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
        backButton.setPosition(this.width / 2 - 75, this.height - this.barHeight.get() / 2 - 10);
        backButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Config.INSTANCE.save();
        this.minecraft.setScreen(lastScreen);
    }
}
