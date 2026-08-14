package dev.nekotune.mdm.client.gui.config.widgets.input;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

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

public class LinkedListInput extends AbstractScrollWidget {

    private static final int BG_COLOR = 0x75000000;
    private static final int BORDER_COLOR = 0x75FFFFFF;

    private static interface Components {
        public static final Component ADD = Component.literal("+")
                .withStyle(ChatFormatting.BOLD);
        public static final Component REMOVE = Component.literal("-")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.RED);
    }

    private Font font;
    private Predicate<String> inputValidator = $ -> true;
    private final LinkedList<EditBox> items = new LinkedList<>();
    private final ListHeaderWidget header;
    private final Button addButton;
    private ScrollListContent content;
    public boolean collapsed = true;

    public LinkedListInput(final int x, final int y, final int width, final int height,
            final Font font, final Component message) {
        super(x, y, width, height, message);
        this.font = font;
        this.header = new ListHeaderWidget(x, y, width, Button.DEFAULT_HEIGHT,
                message, font, () -> this.collapsed);
        this.addButton = Button.builder(Components.ADD, $ -> this.addNewItem())
                .size(Button.DEFAULT_HEIGHT * 3, Button.DEFAULT_HEIGHT)
                .build();
        refreshContents();
    }

    public List<String> getValues() {
        return items.stream().map(editBox -> editBox.getValue()).toList();
    }

    public void setFilter(final Predicate<String> validator) {
        this.inputValidator = validator;
    }

    protected void refreshContents() {
        final var contentBuilder = new ScrollListContent.Builder(width, font);
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
    }

    protected void addNewItem() {
        final EditBox newItem = new EditBox(this.font, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT,
                Component.empty());
        newItem.setFilter(this.inputValidator);
        this.items.add(newItem);
        refreshContents();
    }

    protected void removeItem(final int index) {
        this.items.remove(index);
        refreshContents();
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (this.header.isMouseOver(mouseX, mouseY)) {
            this.collapsed = !this.collapsed;
            handled = true;
        }
        return handled;
    }

    @Override
    protected int getInnerHeight() {
        return this.collapsed ? Button.DEFAULT_HEIGHT : this.content.container().getHeight();
    }

    @Override
    public void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        if (!this.visible)
            return;
        this.renderBackground(guiGraphics);
        this.header.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render contents & scroll bar if not collapsed
        if (!this.collapsed) {
            guiGraphics.enableScissor(this.getX() + 1, this.getY() + 1,
                    this.getX() + this.width - 1, this.getY() + this.height - 1);
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
    protected void renderBackground(final GuiGraphics guiGraphics) {
        guiGraphics.fill(0, 1, this.width, this.getInnerHeight() - 1, BG_COLOR);
        guiGraphics.hLine(0, width, 0, BORDER_COLOR);
        guiGraphics.hLine(0, width, this.getInnerHeight(), BORDER_COLOR);
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
