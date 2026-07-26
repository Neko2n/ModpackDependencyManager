package com.nekotune.mdm;

import com.nekotune.mdm.platform.ForgeConfigHelper;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class ModpackDependencyManager {

    public ModpackDependencyManager(FMLJavaModLoadingContext context) {
        CommonClass.init(this.getClass());

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        Constants.LOG.info("Hello Forge world!");

        context.registerConfig(ModConfig.Type.COMMON, ForgeConfigHelper.FORGE_SPEC);
    }
}