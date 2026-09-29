package dev.nekotune.lace;

import dev.nekotune.lace.platform.PlatformEvents;
import dev.nekotune.lace.platform.Services;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class FabricLoader implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Services.init(this.getClass().getClassLoader());
        Lace.init();
        ServerLifecycleEvents.SERVER_STARTING.register(PlatformEvents.SERVER_STARTING.controller::post);
    }
}
