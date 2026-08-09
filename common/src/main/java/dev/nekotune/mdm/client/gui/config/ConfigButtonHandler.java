package dev.nekotune.mdm.client.gui.config;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import dev.nekotune.mdm.Config;
import dev.nekotune.mdm.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

/**
 * Button on the title screen which opens the configuration screen.
 * 
 * @see MainConfigScreen
 */
public class ConfigButtonHandler {

    protected static final ResourceLocation SPRITE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "config/button");
    protected static final Tooltip TOOLTIP = Tooltip.create(MainConfigScreen.TITLE.copy()
            .setStyle(Style.EMPTY));

    public static final Supplier<SpriteIconButton> BUTTON = () -> {
        final var button = SpriteIconButton.builder(Component.empty(), ConfigButtonHandler::click, true)
                .size(20, 20)
                .sprite(SPRITE, 20, 20)
                .build();
        button.setTooltip(TOOLTIP);
        return button;
    };

    public static void click(final Button button) {
        final Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new MainConfigScreen(mc.screen));
    }

    /**
     * A list of GUI elements that the button should bind to, stored as keys.
     */
    private static final List<String> bindTargets = List.of(
            Component.translatable("menu.options").getString(),
            Component.translatable("fml.menu.mods").getString());

    /**
     * Loads the title screen configuration button widget.
     * 
     * @param mc        The minecraft instance
     * @param listeners GUI listeners to try registering the button to
     * @param adder     Function to add the newly created button widget to the GUI
     */
    public static void init(final Minecraft mc,
            final List<? extends GuiEventListener> listeners,
            final Consumer<SpriteIconButton> adder) {
        if (!(mc.screen instanceof TitleScreen || mc.screen instanceof PauseScreen))
            return;
        if (Config.INSTANCE.production)
            return;
        for (final GuiEventListener listener : listeners) {
            if (!(listener instanceof final AbstractWidget widget))
                continue;
            final String name = widget.getMessage().getString();
            if (!bindTargets.contains(name))
                continue;
            final int spacing = 4;
            final var offset = Config.INSTANCE.buttonOffset;
            final int x = widget.getX() + widget.getWidth() + spacing + offset.x;
            final int y = widget.getY() + offset.y;
            final var button = BUTTON.get();
            button.setPosition(x, y);
            adder.accept(button);
            return;
        }
    }
}
