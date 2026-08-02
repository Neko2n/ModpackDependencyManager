package com.nekotune.mdm;

import com.nekotune.mdm.platform.Services;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Constants.MOD_ID)
public class ModpackDependencyManager {

    public ModpackDependencyManager(final ModContainer mod) {
        Services.init(this.getClass().getClassLoader());
        CommonClass.init();

        // Hook up configuration screen to the mods menu button
        if (FMLEnvironment.dist == Dist.CLIENT)
            NeoForgeConfigScreen.register(mod);
    }
}