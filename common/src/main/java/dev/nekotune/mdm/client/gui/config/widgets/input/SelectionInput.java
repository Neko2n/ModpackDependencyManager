package dev.nekotune.mdm.client.gui.config.widgets.input;

import java.util.LinkedList;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class SelectionInput<T extends Enum<T>> extends Button {

    private final LinkedList<T> values;
    private int index = 0;

    public SelectionInput(final int x, final int y, final int width, final int height,
            final Set<T> values, final T defaultValue, final Button.OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.values = new LinkedList<>(values);
        this.index = this.values.indexOf(defaultValue);
        this.setMessage(this.getMessage());
    }

    public T getValue() {
        return this.values.get(this.index);
    }

    @Override
    public void onPress() {
        super.onPress();
        this.index = (this.index + 1) % this.values.size();
        this.setMessage(this.getMessage());
    }

    @Override
    public Component getMessage() {
        return Component.literal(this.getValue().name()
                .replace('_', ' '))
                .withStyle(ChatFormatting.BOLD);
    }
}
