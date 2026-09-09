package dev.nekotune.mdm.client.gui.config.widgets.container;

import java.util.function.Consumer;

import dev.nekotune.mdm.Resources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

// TODO fix scrolling not working when the mouse is over a dropdown widget
/**
 * Widget which contains a Layout of child widgets within a clickable dropdown.
 */
public class DropdownContainerWidget extends AbstractWidget implements IContainerWidget<Layout> {

    public static final int MARGIN_LINE_COLOR = 0xFFFFFFFF;
    
    private final DropdownContainerWidget.OnClick onClick;
    protected final StringWidget headerText;
    private Layout content = LinearLayout.vertical();
    protected Font font;
    private int headerHeight;
    private int contentMargin = 24;
    private boolean collapsed = true;

    public DropdownContainerWidget(final int x, final int y, final int width, final int height,
            final Component header, final Font font, final DropdownContainerWidget.OnClick onClick) {
        super(x, y, width, height, header);
        this.headerHeight = height;
        this.font = font;
        this.onClick = onClick;
        this.headerText = new StringWidget(header, font);
    }

    @Override
    public Layout getContent() {
        return this.content;
    }

    /**
     * Sets this dropdown's content to a new value.
     */
    public void setContent(final Layout content) {
        this.content = content;
        this.updateContent();
    }

    @Override
    public void updateContent() {
        this.getContent().setPosition(this.getX() + this.getContentMargin(), this.getY() + this.getHeaderHeight());
        this.getContent().arrangeElements();
        this.setHeight(this.getHeaderHeight() + (this.isCollapsed() ? 0 : this.getContent().getHeight()));
    }

    // Match children positions to parent
    @Override
    public void setX(final int x) {
        super.setX(x);
        this.updateContent();
    }

    // Match children positions to parent
    @Override
    public void setY(final int y) {
        super.setY(y);
        this.updateContent();
    }

    /**
     * Sets the height of the dropdown's header.
     */
    public void setHeaderHeight(final int headerHeight) {
        this.headerHeight = headerHeight;
        this.updateContent();
    }

    /**
     * @return The height of the dropdown's header.
     */
    public int getHeaderHeight() {
        return this.headerHeight;
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
        this.updateContent();
    }

    /**
     * Renders the inner contents contained within this dropdown.
     */
    protected void renderContent(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        this.getContent().visitWidgets(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    /**
     * Renders the margin line.
     */
    protected void renderDecorations(final GuiGraphics guiGraphics) {
        final int marginLineX = this.getX() + ((this.getContentMargin() - 4) / 2);
        final int marginLineY = this.getY() + this.getHeaderHeight();
        guiGraphics.vLine(marginLineX, marginLineY, marginLineY + this.getContent().getHeight(),
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
        if (!this.isCollapsed()) {
            this.renderContent(guiGraphics, mouseX, mouseY, partialTick);
            this.renderDecorations(guiGraphics);
        }

        // Render header
        final int centerY = this.getY() + this.getHeaderHeight() / 2;
        final Resources.Sprite arrowSprite = Resources.Gui.Sprites.Icons.Dropdown.get(
                !this.isCollapsed(), this.isHovered());
        guiGraphics.blitSprite(arrowSprite.location(),
                this.getX(), centerY - arrowSprite.height() / 2,
                arrowSprite.width(), arrowSprite.height());
        this.headerText.setPosition(this.getX() + getContentMargin() + arrowSprite.width(),
                centerY - this.headerText.getHeight() / 2);
        this.headerText.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
    }

    // Restrict onClick fire requirements to be clicking the header.
    @Override
    protected boolean clicked(final double mouseX, final double mouseY) {
        if (mouseY > this.getY() + this.getHeaderHeight())
            return false;
        return super.clicked(mouseX, mouseY);
    }

    // Toggle collapsed state when clicked.
    @Override
    public void onClick(final double mouseX, final double mouseY) {
        super.onClick(mouseX, mouseY);
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        this.setCollapsed(!this.isCollapsed());
        this.updateContent();
        this.onClick.accept(this.isCollapsed());
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        final boolean handled;
        if (this.isCollapsed()) {
            handled = false;
        } else {
            handled = this.handleElementInteract(this.getContent(),
                    widget -> widget.mouseClicked(mouseX, mouseY, button));
        }
        return super.mouseClicked(mouseX, mouseY, button) || handled;
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        final boolean handled;
        if (this.isCollapsed()) {
            handled = false;
        } else {
            handled = this.handleElementInteract(this.getContent(),
                    widget -> widget.mouseReleased(mouseX, mouseY, button));
        }
        return super.mouseReleased(mouseX, mouseY, button) || handled;
    }

    @Override
    public boolean charTyped(final char codePoint, final int modifiers) {
        final boolean handled;
        if (this.isCollapsed()) {
            handled = false;
        } else {
            handled = this.handleElementInteract(this.getContent(),
                    widget -> widget.isFocused() && widget.charTyped(codePoint, modifiers));
        }
        return super.charTyped(codePoint, modifiers) || handled;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        final boolean handled;
        if (this.isCollapsed()) {
            handled = false;
        } else {
            handled = this.handleElementInteract(this.getContent(),
                    widget -> widget.isFocused() && widget.keyPressed(keyCode, scanCode, modifiers));
        }
        return super.keyPressed(keyCode, scanCode, modifiers) || handled;
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