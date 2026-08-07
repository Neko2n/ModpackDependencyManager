package dev.nekotune.mdm.client.gui.config;

import java.util.List;
import java.util.function.Consumer;

import dev.nekotune.mdm.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

/**
 * Button on the title screen which opens the configuration screen.
 * @see ConfigScreen
 */
public class ConfigButton extends Button {

    public ConfigButton(final int x, final int y) {
        super(x, y, 20, 20, Component.literal("s"), ConfigButton::click, Button.DEFAULT_NARRATION);
    }

    public static void click(final Button button) {
        final Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new ConfigScreen(mc.screen));
    }

    /**
     * A list of GUI elements that the button should bind to, stored as keys.
     */
    private static final List<String> bindTargets = List.of(
            Component.translatable("menu.options").getString(),
            Component.translatable("fml.menu.mods").getString());
    
    /**
     * Loads the title screen configuration button widget.
     * @param mc The minecraft instance
     * @param listeners GUI listeners to try registering the button to
     * @param adder Function to add the newly created button widget to the GUI
     */
    public static void setupConfigButton(final Minecraft mc,
            final List<? extends GuiEventListener> listeners,
            final Consumer<ConfigButton> adder) {
        if (!(mc.screen instanceof TitleScreen || mc.screen instanceof PauseScreen))
            return;
        for (final GuiEventListener listener : listeners) {
            if (!(listener instanceof final AbstractWidget widget))
                continue;
            final String name = widget.getMessage().getString();
            if (!bindTargets.contains(name))
                continue;
            final int spacing = 4;
            final var offset = Config.INSTANCE.buttonOffset;
            final int x = widget.getX() + widget.getWidth() + spacing + offset.x();
            final int y = widget.getY() + offset.y();
            adder.accept(new ConfigButton(x, y));
            return;
        }
    }
}
