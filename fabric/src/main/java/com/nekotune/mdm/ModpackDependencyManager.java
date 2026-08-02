package com.nekotune.mdm;

import com.nekotune.mdm.platform.Services;

import net.fabricmc.api.ModInitializer;

public class ModpackDependencyManager implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Services.init(this.getClass().getClassLoader());
        CommonClass.init();
    }
}
