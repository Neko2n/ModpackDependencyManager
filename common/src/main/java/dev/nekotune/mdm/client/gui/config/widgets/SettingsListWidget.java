package dev.nekotune.mdm.client.gui.config.widgets;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Config screen widget which renders modifiable settings in a scrolling list.
 */
public class SettingsListWidget extends AbstractScrollWidget {

    protected static final int HORIZONTAL_PADDING = 12;

    protected final ListContent content;

    public SettingsListWidget(final int x, final int y, final int width, final int height,
            final ListContent content) {
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
        final boolean handled = this.handleElementInteract(this.content.container(), widget ->
                widget.isFocused() && widget.charTyped(codePoint, modifiers));
        return super.charTyped(codePoint, modifiers) || handled;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        final boolean handled = this.handleElementInteract(this.content.container(), widget ->
                widget.isFocused() && widget.keyPressed(keyCode, scanCode, modifiers));
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
     * @param element The element to handle interaction detection for. Recursively handles its widgets.
     * @param handler The handler function to apply.
     * @return True if any widgets were handled, false otherwise.
     */
    private boolean handleElementInteract(final LayoutElement element, final Function<AbstractWidget, Boolean> handler) {
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

    /**
     * Data type representing the widget's list object.
     * @param container The container layout holding the list's contents.
     * @param narration The narration to be applied to the list's contents.
     */
    public static record ListContent(Layout container, Component narration) {

        private static final int PADDING = 16;
        private static final int ELEMENT_HEIGHT = Button.DEFAULT_HEIGHT;

        public static class Builder {
            private final Font font;
            private final int width;
            private final LinearLayout container;
            private final MutableComponent narration = Component.empty();

            public Builder(final int width, final Font font) {
                this.font = font;
                this.width = width - HORIZONTAL_PADDING * 2;
                this.container = LinearLayout.vertical();
                this.container.defaultCellSetting().alignHorizontallyCenter();
                this.container.addChild(SpacerElement.width(width - HORIZONTAL_PADDING * 2));
                this.container.addChild(SpacerElement.height(PADDING / 2));
            }

            public Builder addSetting(final Setting setting) {
                return addElement(new Setting.Label(setting.translationKey(), font),
                        setting.createInput(font));
            }

            public Builder addWidget(final String translationKey, final AbstractWidget widget) {
                return addElement(new Setting.Label(translationKey, font), widget);
            }

            private Builder addElement(final StringWidget label, final LayoutElement element) {
                final var holder = new FrameLayout(width, ELEMENT_HEIGHT);
                holder.addChild(label,
                        settings -> settings.alignHorizontallyLeft().alignVerticallyMiddle());
                holder.addChild(element,
                        settings -> settings.alignHorizontallyRight().alignVerticallyMiddle());
                holder.arrangeElements();
                this.container.addChild(holder,
                        settings -> settings.paddingBottom(PADDING / 2).paddingTop(PADDING / 2));
                return this;
            }

            public ListContent build() {
                this.container.addChild(SpacerElement.height(PADDING / 2));
                this.container.arrangeElements();
                return new ListContent(this.container, this.narration);
            }
        }
    }

    public static interface Setting {

        public String translationKey();

        public LayoutElement createInput(final Font font);

        public static final class Label extends StringWidget {

            private Label(final String key, final Font font) {
                super(Component.translatable(key), font);
                final Tooltip tooltip = Tooltip.create(Component.translatable(key + ".tooltip"));
                this.setTooltip(tooltip);
            }
        }
    }
}
