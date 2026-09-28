package dev.nekotune.staple;

import dev.nekotune.staple.platform.PlatformEvents;
import dev.nekotune.staple.platform.Services;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class FabricLoader implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Services.init(this.getClass().getClassLoader());
        Staple.init();
        ServerLifecycleEvents.SERVER_STARTING.register(PlatformEvents.SERVER_STARTING.controller::post);
    }
}
