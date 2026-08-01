package com.nekotune.mdm.definition.gui;

import com.nekotune.mdm.Constants;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {

    public static final Component TITLE = Component.translatable(
            Constants.MOD_ID + ".config.title");

    public final Screen lastScreen;

    public ConfigScreen(final Screen lastScreen) {
        super(TITLE);
        this.lastScreen = lastScreen;
    }
    
}
