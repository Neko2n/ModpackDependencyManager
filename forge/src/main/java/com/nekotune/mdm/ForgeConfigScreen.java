package com.nekotune.mdm;

import com.nekotune.mdm.definition.gui.ConfigScreen;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@OnlyIn(value = Dist.CLIENT)
public final class ForgeConfigScreen extends ConfigScreenHandler {

    public static void register(final FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((mc, lastScreen) -> {
                return new ConfigScreen(lastScreen);
            }));
    }
}