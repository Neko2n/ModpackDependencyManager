package com.nekotune.mdm;

import com.nekotune.mdm.definition.gui.ConfigScreen;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class NeoForgeConfigScreen implements IConfigScreenFactory {

    public static void register(final ModContainer mod) {
        mod.registerExtensionPoint(
            IConfigScreenFactory.class,
            NeoForgeConfigScreen::new);
    }

    @Override
    public Screen createScreen(final ModContainer mod,
            final Screen lastScreen) {
        return new ConfigScreen(lastScreen);
    }
}