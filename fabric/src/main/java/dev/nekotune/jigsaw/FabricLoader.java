package dev.nekotune.jigsaw;

import dev.nekotune.jigsaw.platform.PlatformEvents;
import dev.nekotune.jigsaw.platform.Services;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class FabricLoader implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Services.init(this.getClass().getClassLoader());
        Jigsaw.init();
        ServerLifecycleEvents.SERVER_STARTING.register(PlatformEvents.SERVER_STARTING.controller::post);
    }
}
