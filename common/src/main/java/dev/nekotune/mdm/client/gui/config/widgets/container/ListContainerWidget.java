package dev.nekotune.mdm.client.gui.config.widgets.container;

import java.util.Collection;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
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
 * Renders a list of widgets as a vertical layout, forwarding interactions automatically.
 */
public class ListContainerWidget extends AbstractWidget implements IContainerWidget<LinearLayout> {
    
    protected ListContent content = ListContent.EMPTY;

    public ListContainerWidget(final int x, final int y, final int width, final int height) {
        super(x, y, width, height, Component.empty());
    }

    @Override
    public void updateContent() {
        this.content.container().setX(this.getX());
        this.content.container().setY(this.getY());
        this.content.container().arrangeElements();
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

    @Override
    public LinearLayout getContent() {
        return this.content.container();
    }

    /**
     * Sets this scroll list's content to a new value.
     */
    public void setContent(final ListContent content) {
        this.content = content;
        updateContent();
    }

    protected boolean withinContentAreaPoint(final double x, final double y) {
        return x >= (double)this.getX() && x < (double)(this.getX() + this.width) && y >= (double)this.getY() && y < (double)(this.getY() + this.height);
    }

    @Override
    public int getHeight() {
        return this.content.container().getHeight();
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
    }

    protected final void renderContents(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        this.content.container()
                .visitWidgets(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

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
                widget -> widget.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button) || handled;
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        final boolean handled = this.handleElementInteract(this.content.container(),
                widget -> widget.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button) || handled;
    }

    /**
     * Data type representing the widget's list object.
     * 
     * @param container The container layout holding the list's contents.
     * @param narration The narration label for the container.
     */
    public record ListContent(LinearLayout container, Component narration) {

        public static final int SPACING = 16;
        public static final int ELEMENT_HEIGHT = Button.DEFAULT_HEIGHT;

        public static final ListContent EMPTY = new ListContent(LinearLayout.vertical(), Component.empty());

        public static class Builder {
            private final Font font;
            private final int width;
            private final LinearLayout container;
            private final MutableComponent narration = Component.empty();

            public Builder(final int width, final Font font) {
                this.font = font;
                this.width = width;
                this.container = LinearLayout.vertical();
                this.container.defaultCellSetting().alignHorizontallyCenter();
                this.container.addChild(SpacerElement.width(this.width));
                this.container.addChild(SpacerElement.height(SPACING / 2));
            }

            public Builder addButton(final String translationKey, final Button.OnPress onPress) {
                final Component label = Component.translatable(translationKey);
                final Button button = Button
                        .builder(label.copy().withStyle(ChatFormatting.BOLD), onPress)
                        .size(width, ELEMENT_HEIGHT)
                        .tooltip(Tooltip.create(Component.translatable(translationKey + ".tooltip")))
                        .build();
                return addElement(button, label);
            }

            public Builder addLabeled(final String translationKey, final LayoutElement element) {
                final Component label = Component.translatable(translationKey);
                final var labelWidget = new StringWidget(label, font);
                labelWidget.setTooltip(Tooltip.create(Component.translatable(translationKey + ".tooltip")));
                return addLine(List.of(labelWidget), List.of(element), label);
            }

            public Builder addLine(final Collection<LayoutElement> left, final Collection<LayoutElement> right,
                    final Component narration) {
                final var holder = new FrameLayout(width, ELEMENT_HEIGHT);

                final LinearLayout leftGroup = LinearLayout.horizontal();
                for (final LayoutElement element : left) {
                    leftGroup.addChild(element, settings -> settings.alignVerticallyMiddle());
                }
                holder.addChild(leftGroup, settings -> settings.alignHorizontallyLeft().alignVerticallyMiddle());

                final LinearLayout rightGroup = LinearLayout.horizontal();
                for (final LayoutElement element : right) {
                    rightGroup.addChild(element, settings -> settings.alignVerticallyMiddle());
                }
                holder.addChild(rightGroup, settings -> settings.alignHorizontallyRight().alignVerticallyMiddle());

                this.container.addChild(holder, settings -> settings.paddingBottom(SPACING / 2).paddingTop(SPACING / 2));
                this.narration.append(narration);
                return this;
            }

            public Builder addLine(final LayoutElement left, final LayoutElement right, final Component narration) {
                return this.addLine(List.of(left), List.of(right), narration);
            }

            public Builder addElement(final LayoutElement element, final Component narration) {
                return this.addLine(List.of(element), List.of(), narration);
            }

            public Builder addElement(final AbstractWidget widget) {
                return this.addLine(List.of(widget), List.of(), widget.getMessage());
            }

            public Builder addElements(final Collection<LayoutElement> elements, final Component narration) {
                return this.addLine(elements, List.of(), narration);
            }

            public ListContent build() {
                this.container.addChild(SpacerElement.height(SPACING / 2));
                arrangeNested(this.container);
                return new ListContent(this.container, this.narration);
            }

            private static void arrangeNested(final Layout layout) {
                layout.arrangeElements();
                layout.visitChildren((final LayoutElement child) -> {
                    if (child instanceof final Layout nested && child != layout) {
                        arrangeNested(nested);
                    }
                });
            }
        }
    }
}
