package com.nekotune.mdm;

import com.nekotune.mdm.platform.NeoForgeConfigHelper;
import com.nekotune.mdm.platform.Services;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Constants.MOD_ID)
public class ModpackDependencyManager {

    public ModpackDependencyManager(IEventBus eventBus, ModContainer modContainer) {
        Services.init(this.getClass().getClassLoader());
        if (Services.CONFIG.get() instanceof final NeoForgeConfigHelper config) {
            modContainer.registerConfig(ModConfig.Type.CLIENT,
                    config.loaderSpec,
                    Constants.CONFIG_FILE_NAME);
        }
        CommonClass.init();

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello NeoForge world!");
    }
}