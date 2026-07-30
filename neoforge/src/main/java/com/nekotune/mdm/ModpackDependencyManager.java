package com.nekotune.mdm;

import com.nekotune.mdm.platform.Services;

import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ModpackDependencyManager {

    public ModpackDependencyManager() {
        Services.init(this.getClass().getClassLoader());
        CommonClass.init();
    }
}