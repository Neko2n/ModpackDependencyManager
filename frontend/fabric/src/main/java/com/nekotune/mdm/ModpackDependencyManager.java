package com.nekotune.mdm;

import com.nekotune.mdm.platform.Services;

import net.fabricmc.api.ClientModInitializer;

public class ModpackDependencyManager implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        Services.init(this.getClass().getClassLoader());
        CommonClass.init();
        Constants.LOG.info("Hello Fabric world!");
    }
}
