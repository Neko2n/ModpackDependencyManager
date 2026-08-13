package dev.nekotune.mdm.client.gui.config.widgets;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Config screen widget which renders modifiable settings in a scrolling list.
 */
public class SettingsListWidget extends AbstractScrollWidget {

    protected static final int HORIZONTAL_PADDING = 12;

    protected final ScrollListContent content;

    public SettingsListWidget(final int x, final int y, final int width, final int height,
            final ScrollListContent content) {
        super(x, y, width, height, Component.empty());
        this.content = content;
        updateContent();
    }

    @Override
    protected int getInnerHeight() {
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
                this.getX() + this.width - 1, this.getY() + this.height - 1);
        this.content.container()
                .visitWidgets(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
        guiGraphics.disableScissor();
        this.renderDecorations(guiGraphics);
    }

    @Override
    protected final void renderContents(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
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
        if (!this.visible)
            return false;

        final boolean onScrollbar = this.scrollbarVisible()
                && mouseX >= this.getX() + this.width
                && mouseX <= this.getX() + this.width + this.scrollbarWidth()
                && mouseY >= this.getY()
                && mouseY < this.getY() + this.height;
        if (onScrollbar && button == 0)
            return super.mouseClicked(mouseX, mouseY, button);

        if (!this.withinContentAreaPoint(mouseX, mouseY))
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

    /**
     * Helper method to handle interaction detection for child widgets.
     * 
     * @param element The element to handle interaction detection for. Recursively
     *                handles its widgets.
     * @param handler The handler function to apply.
     * @return True if any widgets were handled, false otherwise.
     */
    private boolean handleElementInteract(final LayoutElement element,
            final Function<AbstractWidget, Boolean> handler) {
        if (element instanceof final AbstractWidget widget)
            return handler.apply(widget);
        final AtomicBoolean handled = new AtomicBoolean(false);
        element.visitWidgets((final AbstractWidget child) -> {
            if (handleElementInteract(child, handler)) {
                handled.set(true);
            }
        });
        return handled.get();
    }

    /**
     * Updates the positions/arrangement of the list content.
     */
    protected void updateContent() {
        final int baseX = this.getX() + this.innerPadding() + HORIZONTAL_PADDING;
        final int scrolledY = this.getY() + this.innerPadding() - ((int) this.scrollAmount());
        this.content.container().setX(baseX);
        this.content.container().setY(scrolledY);
        this.content.container().arrangeElements();
    }
}
