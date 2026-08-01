package com.nekotune.mdm;

import com.nekotune.mdm.definition.gui.ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.minecraft.client.gui.screens.Screen;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<ConfigScreen> getModConfigScreenFactory() {
        return new ConfigScreenFactory<>(){

            @Override
            public ConfigScreen create(final Screen lastScreen) {
                return new ConfigScreen(lastScreen);
            }
        };
    }
}
