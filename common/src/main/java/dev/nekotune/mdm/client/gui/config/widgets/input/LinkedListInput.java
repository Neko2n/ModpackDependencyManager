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
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class LinkedListInput extends AbstractScrollWidget {

    private static interface Components {
        public static final Component ADD = Component.literal("+")
                .withStyle(ChatFormatting.BOLD);
        public static final Component REMOVE = Component.literal("-")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.RED);
    }

    private Font font;
    private Predicate<String> inputValidator = $ -> true;
    private final LinkedList<EditBox> items = new LinkedList<>();
    private final StringWidget header;
    private final Button addButton;
    private ScrollListContent content;
    public boolean collapsed = false;

    public LinkedListInput(final int x, final int y, final int width, final int height,
            final Font font, final Component message) {
        super(x, y, width, height, message);
        this.font = font;
        this.header = new StringWidget(message, font);
        this.addButton = Button.builder(Components.ADD, $ -> this.addNewItem())
                .size(Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT)
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
        return this.content.container().getHeight();
    }

    @Override
    protected void renderContents(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        this.header.setFocused(this.header.isMouseOver(mouseX, mouseY));

        // TODO Hide when collapsed
        // TODO Draw arrow and lines denoting whether the list is collapsed
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
