package dev.nekotune.mdm.client.gui.config.widgets.container;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

// TODO Split into a ListContainerWidget widget class and ScrollContainerWidget wrapper class
/**
 * Config screen widget which renders modifiable settings in a scrolling list.
 */
public class ScrollListWidget extends AbstractScrollWidget implements IContainerWidget {

    protected ListContent content = ListContent.EMPTY;

    public ScrollListWidget(final int x, final int y, final int width, final int height) {
        super(x, y, width, height, Component.empty());
    }

    /**
     * Updates the positions/arrangement of the list content.
     */
    protected void updateContent() {
        final int baseX = this.getX() + this.innerPadding();
        final int scrolledY = this.getY() + this.innerPadding() - ((int) this.scrollAmount());
        this.content.container().setX(baseX);
        this.content.container().setY(scrolledY);
        this.content.container().arrangeElements();
    }

    /**
     * Sets this scroll list's content to a new value.
     */
    public void setContent(final ListContent content) {
        this.content = content;
        updateContent();
    }

    @Override
    public int getInnerHeight() {
        return this.content.container().getHeight();
    }

    @Override
    protected double scrollRate() {
        return 9.0d;
    }

    @Override
    protected void setScrollAmount(final double scrollAmount) {
        super.setScrollAmount(scrollAmount);
        updateContent();
    }

    @Override
    public void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        if (!this.visible)
            return;
        this.renderBackground(guiGraphics);
        guiGraphics.enableScissor(this.getX() + 1, this.getY() + 1,
                this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1);
        this.renderContents(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.disableScissor();
        this.renderDecorations(guiGraphics);
    }

    @Override
    protected final void renderContents(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        this.content.container()
                .visitWidgets(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    protected void renderBackground(final GuiGraphics guiGraphics) {
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationOutput) {
        narrationOutput.add(NarratedElementType.TITLE, this.content.narration());
    }

    @Override
    public boolean charTyped(final char codePoint, final int modifiers) {
        final boolean handled = this.handleElementInteract(this.content.container(),
                widget -> widget.isFocused() && widget.charTyped(codePoint, modifiers));
        return super.charTyped(codePoint, modifiers) || handled;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        final boolean handled = this.handleElementInteract(this.content.container(),
                widget -> widget.isFocused() && widget.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers) || handled;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (!this.visible || !this.withinContentAreaPoint(mouseX, mouseY))
            return false;
        final boolean handled = this.handleElementInteract(this.content.container(),
                widget -> {
                    final boolean widget$handled = widget.mouseClicked(mouseX, mouseY, button);
                    if (widget instanceof EditBox)
                        widget.setFocused(widget$handled);
                    return widget$handled;
                });
        return super.mouseClicked(mouseX, mouseY, button) || handled;
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        final boolean handled = this.handleElementInteract(this.content.container(),
                widget -> widget.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button) || handled;
    }

    // Expose visibility
    @Override
    public int innerPadding() {
        return super.innerPadding();
    }

    // Expose visibility
    @Override
    public int totalInnerPadding() {
        return super.totalInnerPadding();
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
}
