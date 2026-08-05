package dev.nekotune.mdm;

import dev.nekotune.mdm.platform.PlatformEvents;
import dev.nekotune.mdm.platform.Services;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class ModpackDependencyManager implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Services.init(this.getClass().getClassLoader());
        CommonClass.init();
        ServerLifecycleEvents.SERVER_STARTING.register(PlatformEvents.SERVER_STARTING.controller::post);
    }
}
