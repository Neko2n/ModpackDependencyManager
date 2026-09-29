package dev.nekotune.lace.client;

import dev.nekotune.lace.Constants;
import dev.nekotune.lace.platform.PlatformEvents;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class ClientNeoForgeLoader {
    
    public ClientNeoForgeLoader() {
        ClientLace.init();
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onScreenInitPost(final ScreenEvent.Init.Post event) {
        PlatformEvents.SCREEN_INIT.controller.post(event.getScreen());
    }
}
