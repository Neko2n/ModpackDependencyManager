package com.nekotune.mdm;

import com.nekotune.mdm.platform.FabricConfigHelper;
import com.nekotune.mdm.platform.Services;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class ModpackDependencyManager implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        Services.init(this.getClass().getClassLoader());
        if (Services.CONFIG.get() instanceof final FabricConfigHelper config) {
            NeoForgeConfigRegistry.INSTANCE.register(
                Constants.MOD_ID,
                ModConfig.Type.CLIENT,
                config.loaderSpec,
                Constants.CONFIG_FILE_NAME
            );
            ConfigScreenFactoryRegistry.INSTANCE.register(
                    Constants.MOD_ID, ConfigurationScreen::new);
        }
        CommonClass.init();
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");
    }
}
