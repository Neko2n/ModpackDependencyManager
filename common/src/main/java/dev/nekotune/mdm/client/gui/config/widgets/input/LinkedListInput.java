package dev.nekotune.mdm.client.gui.config.widgets.input;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.client.gui.config.widgets.ScrollListContent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

// TODO Fix widget being totally broken and just taking up space without rendering anything
public class LinkedListInput extends AbstractScrollWidget {

    private static final int BORDER_COLOR = 0x75FFFFFF;

    private static interface Components {
        public static final Component ADD = Component.literal("+")
                .withStyle(ChatFormatting.BOLD);
        public static final Component REMOVE = Component.literal("-")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.RED);
    }

    private Font font;
    private Predicate<String> inputValidator = $ -> true;
    private Consumer<Collection<String>> responder = $ -> {
    };
    private final LinkedList<EditBox> items = new LinkedList<>();
    private final ListHeaderWidget header;
    private final Button addButton;
    private ScrollListContent content;
    public boolean collapsed = true;
    public Consumer<Boolean> onCollapsedChanged = $ -> {
    };

    public LinkedListInput(final int x, final int y, final int width,
            final Font font, final Component message) {
        super(x, y, width, Button.DEFAULT_HEIGHT, message);
        this.font = font;
        this.header = new ListHeaderWidget(x, y, width, Button.DEFAULT_HEIGHT,
                message, font, () -> this.collapsed);
        this.addButton = Button.builder(Components.ADD, $ -> this.addNewItem(""))
                .size(Button.DEFAULT_HEIGHT * 2, Button.DEFAULT_HEIGHT)
                .build();
        rebuildListContent();
    }

    public List<String> getValues() {
        return items.stream().map(editBox -> editBox.getValue()).toList();
    }

    public void setValues(final Collection<String> values) {
        items.clear();
        for (final String value : values) {
            this.addNewItem(value);
        }
    }

    public void setFilter(final Predicate<String> validator) {
        this.inputValidator = validator;
        for (final EditBox item : this.items) {
            item.setFilter(validator);
        }
    }

    public void setResponder(final Consumer<Collection<String>> responder) {
        this.responder = responder;
    }

    protected void updateContentWidget() {
        final int baseX = this.getX() + this.innerPadding();
        final int baseY = this.getY() + this.header.getHeight() + this.innerPadding();
        final int scrolledY = baseY - ((int) this.scrollAmount());
        this.content.container().setX(baseX + this.header.contentMargin());
        this.content.container().setY(scrolledY);
        this.content.container().arrangeElements();
    }

    protected void rebuildListContent() {
        final int contentWidth = this.width - this.totalInnerPadding() - this.header.contentMargin();
        final var contentBuilder = new ScrollListContent.Builder(contentWidth, font);
        for (int i = 0; i < this.items.size(); i++) {
            final EditBox item = this.items.get(i);
            final int i$immutable = i;
            final Button removeButton = Button.builder(Components.REMOVE,
                    $ -> this.items.remove(i$immutable))
                    .size(Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT)
                    .build();
            contentBuilder.addLine(item, removeButton);
        }
        contentBuilder.addElement(this.addButton);
        this.content = contentBuilder.build();
        updateContentWidget();
    }

    protected void addNewItem(final String value) {
        final int itemWidth = Math.min(Button.DEFAULT_WIDTH, width);
        final EditBox newItem = new EditBox(this.font, itemWidth, Button.DEFAULT_HEIGHT,
                Component.empty());
        newItem.setFilter(this.inputValidator);
        if (this.inputValidator.test(value))
            newItem.setValue(value);
        newItem.setResponder((final String text) -> {
            this.responder.accept(getValues());
        });
        this.items.add(newItem);
        rebuildListContent();
    }

    protected void removeItem(final int index) {
        this.items.remove(index);
        rebuildListContent();
    }

    @Override
    protected void setScrollAmount(final double scrollAmount) {
        super.setScrollAmount(scrollAmount);
        updateContentWidget();
    }

    @Override
    protected int getInnerHeight() {
        return this.collapsed ? 0 : this.content.container().getHeight();
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (this.header.isMouseOver(mouseX, mouseY)) {
            this.collapsed = !this.collapsed;
            Constants.LOG.debug("COLLAPSED CHANGED TO " + collapsed);
            this.setHeight(this.getInnerHeight() + this.header.getHeight());
            this.onCollapsedChanged.accept(this.collapsed);
            handled = true;
        }
        return handled;
    }

    @Override
    public void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        if (!this.visible)
            return;
        this.renderBackground(guiGraphics);
        this.header.setX(this.getX());
        this.header.setY(this.getY());
        this.header.setWidth(this.width);
        this.header.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render contents & scroll bar if not collapsed
        if (!this.collapsed) {
            final int contentY = this.getY() + this.header.getHeight();
            guiGraphics.enableScissor(this.getX() + 1, contentY + 1,
                    this.getX() + this.width - 1, contentY + this.getInnerHeight() - 1);
            this.renderContents(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.disableScissor();
            this.renderDecorations(guiGraphics);
        }
    }

    @Override
    protected final void renderContents(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        this.content.container().visitWidgets(
                widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    protected void renderDecorations(final GuiGraphics guiGraphics) {
        // Draw scroll bar accounting for the header
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, this.header.getHeight(), 0);
        super.renderDecorations(guiGraphics);
        guiGraphics.pose().popPose();

        // Draw margin line when open
        if (this.collapsed)
            return;
        final int marginLineX = this.getX() + ListHeaderWidget.ARROW_SIZE / 2;
        final int marginLineY = this.getY() + this.header.getHeight();
        final int contentHeight = this.content.container().getHeight();
        guiGraphics.vLine(marginLineX, marginLineY, marginLineY + contentHeight, BORDER_COLOR);
    }

    @Override
    protected double scrollRate() {
        return 9D;
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationOutput) {
        narrationOutput.add(NarratedElementType.TITLE, this.content.narration());
    }
}
