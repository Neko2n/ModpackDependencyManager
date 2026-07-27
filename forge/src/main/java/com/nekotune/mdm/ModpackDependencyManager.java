package com.nekotune.mdm;

import com.nekotune.mdm.platform.ForgeConfigHelper;
import com.nekotune.mdm.platform.Services;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class ModpackDependencyManager {

    public ModpackDependencyManager(final FMLJavaModLoadingContext context) {
        Services.init(this.getClass().getClassLoader());
        if (Services.CONFIG.get() instanceof final ForgeConfigHelper config) {
            context.registerConfig(ModConfig.Type.CLIENT,
                    config.loaderSpec,
                    Constants.CONFIG_FILE_NAME);
        }
        CommonClass.init();

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        Constants.LOG.info("Hello Forge world!");
    }
}