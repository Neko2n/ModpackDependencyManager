package dev.nekotune.mdm;

import dev.nekotune.mdm.client.gui.ConfigScreen;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@OnlyIn(value = Dist.CLIENT)
public final class NeoForgeConfigScreen implements IConfigScreenFactory {

    public static void register(final ModContainer mod) {
        mod.registerExtensionPoint(
            IConfigScreenFactory.class,
            new NeoForgeConfigScreen());
    }

    @Override
    public Screen createScreen(final ModContainer mod,
            final Screen lastScreen) {
        return new ConfigScreen(lastScreen);
    }
}