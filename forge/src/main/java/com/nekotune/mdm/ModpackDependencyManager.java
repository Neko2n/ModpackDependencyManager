package com.nekotune.mdm;

import com.nekotune.mdm.client.ClientCommonClass;
import com.nekotune.mdm.client.gui.ConfigScreen;
import com.nekotune.mdm.platform.Services;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class ModpackDependencyManager {

    private final FMLJavaModLoadingContext context;

    public ModpackDependencyManager(final FMLJavaModLoadingContext context) {
        this.context = context;
        Services.init(this.getClass().getClassLoader());
        CommonClass.init();
        
        // Register lifecycle events
        final IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::onClientSetup);
    }
    
    private void onClientSetup(final FMLClientSetupEvent event) {
        ClientCommonClass.init();

        // Hook up configuration screen to the mods menu button
        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, lastScreen) -> {
                    return new ConfigScreen(lastScreen);
                }));
    }
}