package com.nekotune.mdm.client;

import com.nekotune.mdm.platform.PlatformEvents;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.TitleScreen;

@Environment(value = EnvType.CLIENT)
public class ClientModpackDependencyManager implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        ClientCommonClass.init();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                PlatformEvents.CLIENT_LOADED.controller.post(null);
            }
        });
    }
}
