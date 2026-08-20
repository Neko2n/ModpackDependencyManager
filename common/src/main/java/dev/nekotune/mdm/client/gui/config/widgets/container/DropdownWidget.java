package dev.nekotune.mdm.client.gui.config.widgets.container;

import java.util.function.Consumer;

import dev.nekotune.mdm.Constants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Widget which contains another LayoutElement within a clickable dropdown.
 */
public class DropdownWidget<T extends LayoutElement> extends AbstractWidget implements IContainerWidget {

    public static final int ARROW_SIZE = 20;
    public static final int MARGIN_LINE_COLOR = 0xFFFFFFFF;
    
    public final T content;
    private final DropdownWidget.OnClick onClick;
    protected final StringWidget headerText;
    protected int headerHeight;
    protected Font font;
    private int contentMargin = ARROW_SIZE + 4;
    private boolean collapsed = true;

    public DropdownWidget(final int x, final int y, final int width, final int height,
            final Component header, final Font font, final T content,
            final DropdownWidget.OnClick onClick) {
        super(x, y, width, height, header);
        this.headerHeight = height;
        this.font = font;
        this.content = content;
        this.onClick = onClick;
        this.headerText = new StringWidget(header, font);
    }

    /**
     * @return True if the dropdown is collapsed and its contents should be hidden,
     *         false otherwise.
     */
    public boolean isCollapsed() {
        return this.collapsed;
    }

    /**
     * Sets the collapsed state of this dropdown.
     * @param collapsed True if the dropdown should hide its internal content.
     */
    public void setCollapsed(final boolean collapsed) {
        this.collapsed = collapsed;
    }

    /**
     * @return The margin at which to render content, in local space.
     */
    public int getContentMargin() {
        return this.contentMargin;
    }

    /**
     * Sets the margin at which to render the inner content.
     * @param contentMargin The margin offset in pixels from this widget's X-position.
     */
    public void setContentMargin(final int contentMargin) {
        this.contentMargin = contentMargin;
    }

    /**
     * Renders the inner contents contained within this dropdown.
     */
    protected void renderContent(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        content.setPosition(this.getX() + this.getContentMargin(), this.getY() + this.headerHeight);
        content.visitWidgets(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    /**
     * Renders the margin line.
     */
    protected void renderDecorations(final GuiGraphics guiGraphics) {
        final int marginLineX = this.getX() + DropdownWidget.ARROW_SIZE / 2;
        final int marginLineY = this.getY() + this.headerHeight;
        guiGraphics.vLine(marginLineX, marginLineY, marginLineY + this.content.getHeight(),
                MARGIN_LINE_COLOR);
    }

    @Override
    public void setTooltip(final Tooltip tooltip) {
        this.headerText.setTooltip(tooltip);
    }

    @Override
    public void setMessage(final Component message) {
        super.setMessage(message);
        this.headerText.setMessage(message);
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {

        // If not collapsed, render contents and margin line
        if (!this.collapsed) {
            this.renderContent(guiGraphics, mouseX, mouseY, partialTick);
            this.renderDecorations(guiGraphics);
        }

        // Render header
        final int x = this.getX();
        final int y = this.getY();
        final int centerY = y + this.headerHeight / 2;
        guiGraphics.blitSprite(this.isCollapsed() ? ArrowSprites.CLOSED : ArrowSprites.OPEN,
                x, centerY - ARROW_SIZE / 2, ARROW_SIZE, ARROW_SIZE);
        this.headerText.setPosition(x + getContentMargin(), centerY - this.headerText.getHeight() / 2);
        this.headerText.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
    }

    // Restrict onClick fire requirements to be clicking the header.
    @Override
    protected boolean clicked(final double mouseX, final double mouseY) {
        if (mouseY > this.getY() + this.headerHeight)
            return false;
        return super.clicked(mouseX, mouseY);
    }

    // Toggle collapsed state when clicked.
    @Override
    public void onClick(final double mouseX, final double mouseY) {
        super.onClick(mouseX, mouseY);
        this.collapsed = !this.collapsed;
        this.setHeight(this.headerHeight + (this.collapsed ? 0 : this.content.getHeight()));
        this.onClick.accept(this.collapsed);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        final boolean handled;
        if (this.collapsed) {
            handled = false;
        } else {
            handled = this.handleElementInteract(this.content,
                    widget -> {
                        final boolean widget$handled = widget.mouseClicked(mouseX, mouseY, button);
                        if (widget instanceof EditBox)
                            widget.setFocused(widget$handled);
                        return widget$handled;
                    });
        }
        return super.mouseClicked(mouseX, mouseY, button) || handled;
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        final boolean handled = this.handleElementInteract(this.content,
                widget -> widget.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button) || handled;
    }

    @Override
    public boolean charTyped(final char codePoint, final int modifiers) {
        final boolean handled = this.handleElementInteract(this.content,
                widget -> widget.isFocused() && widget.charTyped(codePoint, modifiers));
        return super.charTyped(codePoint, modifiers) || handled;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        final boolean handled = this.handleElementInteract(this.content,
                widget -> widget.isFocused() && widget.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers) || handled;
    }

    public static interface ArrowSprites {
        public static final ResourceLocation OPEN = ResourceLocation.fromNamespaceAndPath(
                Constants.MOD_ID, "icon/arrow_open");
        public static final ResourceLocation CLOSED = ResourceLocation.fromNamespaceAndPath(
                Constants.MOD_ID, "icon/arrow_closed");
    }

    @FunctionalInterface
    public static interface OnClick extends Consumer<Boolean> {

        /**
         * @param collapsedIs The current collapsed state of the dropdown after the
         *                    click.
         */
        @Override
        abstract void accept(final Boolean collapsedIs);
    }
}