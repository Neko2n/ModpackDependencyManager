package com.nekotune.mdm.mixin.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nekotune.mdm.client.ClientCommonClass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

@Mixin(Minecraft.class)
public class TitleScreenMixin {

    private boolean firstLoad = true;
    
    @Inject(method = "setScreen", at = @At("TAIL"))
    private void mdm$onTitleScreen(final Screen screen, final CallbackInfo ci) {
        if (screen instanceof TitleScreen && firstLoad) {
            firstLoad = false;
            ClientCommonClass.clientLoaded((Minecraft)(Object)this);
        }
    }
}
