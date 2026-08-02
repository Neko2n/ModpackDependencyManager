package com.nekotune.mdm;

import com.nekotune.mdm.platform.Services;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(Constants.MOD_ID)
public class ModpackDependencyManager {

    public ModpackDependencyManager(final FMLJavaModLoadingContext context) {
        Services.init(this.getClass().getClassLoader());
        CommonClass.init();

        // Hook up configuration screen to the mods menu button
        if (FMLEnvironment.dist == Dist.CLIENT)
            ForgeConfigScreen.register(context);
    }
}