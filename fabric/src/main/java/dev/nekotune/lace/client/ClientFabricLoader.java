package dev.nekotune.lace.client;

import dev.nekotune.lace.platform.PlatformEvents;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

@Environment(value = EnvType.CLIENT)
public class ClientFabricLoader implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        ClientLace.init();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            PlatformEvents.SCREEN_INIT.controller.post(screen);
        });
    }
}
