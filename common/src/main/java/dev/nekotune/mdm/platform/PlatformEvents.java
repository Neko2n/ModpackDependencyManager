package dev.nekotune.mdm.platform;

import dev.nekotune.mdm.core.Event;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.MinecraftServer;

public interface PlatformEvents {
    /**
     * Event that should be fired when a screen is opened.
     */
    public static final Event<Screen> SCREEN_INIT = new Event<>();

    /**
     * Event that should be fired whenever a MinecraftServer is created.
     */
    public static final Event<MinecraftServer> SERVER_STARTING = new Event<>();

    /**
     * Event that should be fired whenever a MinecraftServer is closing.
     */
    public static final Event<MinecraftServer> SERVER_CLOSING = new Event<>();
}