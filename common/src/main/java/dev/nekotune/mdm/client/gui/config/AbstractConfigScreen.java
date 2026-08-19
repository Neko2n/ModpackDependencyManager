package dev.nekotune.mdm.client.gui.config;

import dev.nekotune.mdm.Config;
import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.client.gui.config.widgets.ScrollListContent;
import dev.nekotune.mdm.client.gui.config.widgets.SettingsListWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * General-purpose screen for rendering configurable settings.
 * Renders a top bar with a title, a scrolling list of settings widgets,
 * and a bottom bar with a button.
 */
public abstract class AbstractConfigScreen extends Screen {

    public static final String KEY = Constants.MOD_ID + ".screen.config";
    public static final Component EXIT_BUTTON = Component
            .translatableWithFallback(KEY + ".button.exit", "Save & Exit");
    public static final Component APPLY_BUTTON = Component
            .translatableWithFallback(KEY + ".button.apply", "Apply");
    protected static final int SCROLL_LIST_PADDING = 80;
    private static final int BAR_BG_COLOR = 0x65000000;

    private final SettingsListWidget scrollList;
    public final Screen lastScreen;
    public final Button backButton;

    protected AbstractConfigScreen(final Component title, final Screen lastScreen) {
        super(title);
        this.lastScreen = lastScreen;
        this.backButton = Button.builder(Component.empty(), $ -> this.onClose())
                .size(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT)
                .build();
        this.scrollList = new SettingsListWidget(0, 0, 0, 0);
    }

    /**
     * Populates the settings list with content provided to the supplied builder.
     * 
     * @param builder The builder to submit settings content to.
     */
    protected abstract void populateSettings(final ScrollListContent.Builder builder);

    /**
     * Re-builds the settings list, re-running
     * {@link AbstractConfigScreen#populateSettings}.
     */
    protected final void rebuildSettings() {
        final int listWidth = this.width - SCROLL_LIST_PADDING * 2;
        this.scrollList.setPosition(SCROLL_LIST_PADDING, this.barHeight());
        this.scrollList.setWidth(listWidth);
        this.scrollList.setHeight(this.height - this.barHeight() * 2);
        final int listContentWidth = listWidth - this.scrollList.totalInnerPadding();
        final var listBuilder = new ScrollListContent.Builder(listContentWidth, this.font);
        populateSettings(listBuilder);
        this.scrollList.setContent(listBuilder.build());
    }

    /**
     * @return True if the screen should call {@link Config#save} upon closing,
     *         false otherwise.
     */
    protected boolean shouldSaveOnClose() {
        return !(this.lastScreen instanceof AbstractConfigScreen);
    }

    /**
     * Renders the title text at the top of the screen.
     */
    public void renderTitle(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        guiGraphics.drawCenteredString(this.font, this.title,
                this.width / 2, this.barHeight() / 2, 0xFFFFFFFF);
    }

    /**
     * Renders the button at the bottom of the screen.
     * By default, this is a button which closes the screen and saves the config.
     */
    public void renderBottomButton(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        if (shouldSaveOnClose()) {
            backButton.setMessage(EXIT_BUTTON);
        } else {
            backButton.setMessage(APPLY_BUTTON);
        }
        backButton.setPosition(this.width / 2 - (backButton.getWidth() / 2),
                this.height - this.barHeight() / 2 - (backButton.getHeight() / 2));
        backButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * Renders the bars at the top and bottom of the screen, as well as their
     * contents.
     * By default, this is a top bar containing the title, and a bottom bar
     * containing the bottom button.
     */
    public void renderBars(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {

        // Draw bar background
        final int barHeight = this.barHeight();
        guiGraphics.fill(0, 0, this.width, barHeight - 1, BAR_BG_COLOR);
        guiGraphics.fill(0, this.height, this.width, this.height - barHeight + 1, BAR_BG_COLOR);
        guiGraphics.hLine(0, this.width, barHeight - 1, 0x95000000);
        guiGraphics.hLine(0, this.width, barHeight, 0x55FFFFFF);
        guiGraphics.hLine(0, this.width, this.height - barHeight, 0x95000000);
        guiGraphics.hLine(0, this.width, this.height - barHeight - 1, 0x55FFFFFF);

        // Draw bar contents
        this.renderTitle(guiGraphics, mouseX, mouseY, partialTick);
        this.renderBottomButton(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * @return The height of the top and bottom bars.
     */
    protected int barHeight() {
        return this.height / 6;
    }

    /**
     * @return The width of the inner scroll list.
     */
    protected int getInnerWidth() {
        return this.scrollList.getWidth();
    }

    /**
     * @return The height of the inner scroll list.
     */
    protected int getInnerHeight() {
        return this.scrollList.getInnerHeight();
    }

    @Override
    protected void init() {
        super.init();
        this.rebuildSettings();
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
        this.renderBars(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.shouldSaveOnClose()) {
            Config.INSTANCE.save();
        }
        this.minecraft.setScreen(lastScreen);
    }
}
