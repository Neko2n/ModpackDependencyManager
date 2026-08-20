package dev.nekotune.mdm.client.gui.config.widgets.input;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.client.gui.config.widgets.container.ListContent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

// TODO Refactor into a subclass of ListWidget
public class OrderedListInput extends LinearLayout {

    public static final String KEY = Constants.Assets.Lang.Gui.Widget.Input.KEY + ".ordered-list";

    public static final Component ADD_TEXT = Component.literal("+")
            .withStyle(ChatFormatting.BOLD);

    private Font font;
    private Predicate<String> inputValidator = $ -> true;
    private Consumer<Collection<String>> responder = $ -> {
    };
    private final LinkedList<EditBox> items = new LinkedList<>();
    private final Button addButton;

    public OrderedListInput(final int width, final LinearLayout.Orientation orientation,
            final Font font) {
        super(width, 0, orientation);
        this.font = font;
        this.addButton = Button.builder(ADD_TEXT, $ -> this.addValue(""))
                .size(Button.DEFAULT_HEIGHT * 2, Button.DEFAULT_HEIGHT)
                .build();
        rebuildContent();
    }

    public final void setFilter(final Predicate<String> validator) {
        this.inputValidator = validator;
        for (final EditBox item : this.items) {
            item.setFilter(validator);
        }
    }

    public final void setResponder(final Consumer<Collection<String>> responder) {
        this.responder = responder;
    }

    public final List<String> getValues() {
        return items.stream().map(editBox -> editBox.getValue()).toList();
    }

    public final void setValues(final Collection<String> values) {
        items.clear();
        for (final String value : values) {
            this.addValue(value);
        }
    }

    protected final void addValue(final String value) {
        final EditBox newItem = new EditBox(this.font, this.getWidth(), Button.DEFAULT_HEIGHT,
                Component.empty());
        newItem.setFilter(this.inputValidator);
        if (this.inputValidator.test(value))
            newItem.setValue(value);
        newItem.setResponder((final String text) -> {
            this.responder.accept(getValues());
        });
        this.items.add(newItem);
        rebuildContent();
    }

    protected final void removeValue(final int index) {
        this.items.remove(index);
        rebuildContent();
    }

    private void rebuildContent() {
        final var contentBuilder = new ListContent.Builder(this.getWidth(), font);
        for (int i = 0; i < this.items.size(); i++) {
            final int i$immutable = i;
            final EditBox item = this.items.get(i$immutable);
            final var deleteIcon = Constants.Assets.Gui.Sprite.Icon.DELETE;
            final SpriteIconButton removeButton = SpriteIconButton.builder(Component.empty(),
                    $ -> this.removeValue(i$immutable), true)
                    .size(Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT)
                    .sprite(deleteIcon.location(), deleteIcon.width(), deleteIcon.height())
                    .build();
            contentBuilder.addLine(item, removeButton, item.getMessage());
        }
        contentBuilder.addElement(this.addButton, Component.translatable(KEY + ".add.narration"));
        final ListContent content = contentBuilder.build();
        this.setHeight(Math.min(this.getInnerHeight() + 1, this.getMaxHeight()));
    }
}
