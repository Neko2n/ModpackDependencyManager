package dev.nekotune.mdm.client.gui.config.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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

    protected void updateContent() {
        final int baseX = this.getX() + this.innerPadding() + HORIZONTAL_PADDING;
        final int scrolledY = this.getY() + this.innerPadding() - ((int) this.scrollAmount());
        this.content.container().setX(baseX);
        this.content.container().setY(scrolledY);
        this.content.container().arrangeElements();
    }

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

            public Builder addSetting(final Component label, final LayoutElement input) {
                final var labelWidget = new StringWidget(label, this.font).alignLeft();
                final var holder = new FrameLayout(this.width, ELEMENT_HEIGHT);
                holder.addChild(labelWidget, settings -> settings.alignHorizontallyLeft().alignVerticallyMiddle());
                holder.addChild(input, settings -> settings.alignHorizontallyRight().alignVerticallyMiddle());
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
}
