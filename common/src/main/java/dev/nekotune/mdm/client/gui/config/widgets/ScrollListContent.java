package dev.nekotune.mdm.client.gui.config.widgets;

import java.util.Collection;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Data type representing the widget's list object.
 * 
 * @param container The container layout holding the list's contents.
 * @param narration The narration label for the container.
 */
public record ScrollListContent(Layout container, Component narration) {

    private static final int SPACING = 16;
    private static final int ELEMENT_HEIGHT = Button.DEFAULT_HEIGHT;

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
            final Button button = Button
                    .builder(Component.translatable(translationKey)
                            .withStyle(ChatFormatting.BOLD), onPress)
                    .size(width, ELEMENT_HEIGHT)
                    .tooltip(Tooltip.create(Component.translatable(translationKey + ".tooltip")))
                    .build();
            return addElement(button);
        }

        public Builder addSetting(final String translationKey, final LayoutElement element) {
            final var label = new StringWidget(Component.translatable(translationKey), font);
            label.setTooltip(Tooltip.create(Component.translatable(translationKey + ".tooltip")));
            return addLine(List.of(label), List.of(element));
        }

        public Builder addLine(final Collection<LayoutElement> left, final Collection<LayoutElement> right) {
            final var holder = new FrameLayout(width, ELEMENT_HEIGHT);

            final LinearLayout leftGroup = LinearLayout.horizontal();
            for (final LayoutElement element : left) {
                leftGroup.addChild(element, settings -> settings.alignVerticallyMiddle());
            }
            leftGroup.arrangeElements();
            holder.addChild(leftGroup, settings -> settings.alignHorizontallyLeft().alignVerticallyMiddle());

            final LinearLayout rightGroup = LinearLayout.horizontal();
            for (final LayoutElement element : right) {
                rightGroup.addChild(element, settings -> settings.alignVerticallyMiddle());
            }
            rightGroup.arrangeElements();
            holder.addChild(rightGroup, settings -> settings.alignHorizontallyRight().alignVerticallyMiddle());

            holder.arrangeElements();
            this.container.addChild(holder, settings -> settings.paddingBottom(SPACING / 2).paddingTop(SPACING / 2));
            return this;
        }

        public Builder addLine(final LayoutElement left, final LayoutElement right) {
            return this.addLine(List.of(left), List.of(right));
        }

        public Builder addElement(final LayoutElement element) {
            return this.addLine(List.of(element), List.of());
        }

        public Builder addElements(final Collection<LayoutElement> elements) {
            return this.addLine(elements, List.of());
        }

        public ScrollListContent build() {
            this.container.addChild(SpacerElement.height(SPACING / 2));
            this.container.arrangeElements();
            return new ScrollListContent(this.container, this.narration);
        }
    }
}