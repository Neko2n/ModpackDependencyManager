package com.nekotune.mdm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nekotune.mdm.CommonClass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

@Mixin(Minecraft.class)
public class TitleScreenMixin {
    
    @Inject(method = "setScreen", at = @At("TAIL"))
    private void mdm$onTitleScreenShown(final Screen screen, final CallbackInfo ci) {
        if (screen instanceof TitleScreen) {
            CommonClass.onTitleScreenShown((Minecraft)(Object)this);
        }
    }
}
