package dev.nekotune.mdm.platform;

import dev.nekotune.mdm.core.Event;
import net.minecraft.server.MinecraftServer;

public interface PlatformEvents {
    /**
     * Event that should be fired once the client reaches the title screen.
     */
    public static final Event.Flag<Void> CLIENT_LOADED = new Event.Flag<>();

    /**
     * Event that should be fired whenever a MinecraftServer is created.
     */
    public static final Event<MinecraftServer> SERVER_STARTING = new Event<>();

    /**
     * Event that should be fired whenever a MinecraftServer is closing.
     */
    public static final Event<MinecraftServer> SERVER_CLOSING = new Event<>();
}