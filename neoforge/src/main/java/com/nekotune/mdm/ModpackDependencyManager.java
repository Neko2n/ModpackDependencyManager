package com.nekotune.mdm;

import com.nekotune.mdm.platform.Services;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ModpackDependencyManager {

    public ModpackDependencyManager(final ModContainer mod) {
        Services.init(this.getClass().getClassLoader());
        CommonClass.init();

        // Hook up configuration screen to the mods menu button
        NeoForgeConfigScreen.register(mod);
    }
}