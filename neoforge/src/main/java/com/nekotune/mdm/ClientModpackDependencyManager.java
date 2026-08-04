package com.nekotune.mdm;

import com.nekotune.mdm.client.ClientCommonClass;
import com.nekotune.mdm.platform.PlatformEvents;

import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class ClientModpackDependencyManager {
    
    public ClientModpackDependencyManager() {
        ClientCommonClass.init();
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onScreenOpening(final ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof TitleScreen) {
            PlatformEvents.CLIENT_LOADED.controller.post(null);
        }
    }
}
