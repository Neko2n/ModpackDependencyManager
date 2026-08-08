package dev.nekotune.mdm;

import dev.nekotune.mdm.client.ClientCommonClass;
import dev.nekotune.mdm.client.gui.config.MainConfigScreen;
import dev.nekotune.mdm.platform.PlatformEvents;
import dev.nekotune.mdm.platform.Services;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
        
        // Register events
        final IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private void onClientSetup(final FMLClientSetupEvent event) {
        ClientCommonClass.init();

        // Hook up configuration screen to the mods menu button
        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, lastScreen) -> {
                    return new MainConfigScreen(lastScreen);
                }));
    }

    @SubscribeEvent
    public static void onScreenInitPost(final ScreenEvent.Init.Post event) {
        PlatformEvents.SCREEN_INIT.controller.post(event.getScreen());
    }

    @SubscribeEvent
    public void onServerStarting(final ServerStartingEvent event) {
        PlatformEvents.SERVER_STARTING.controller.post(event.getServer());
    }
}