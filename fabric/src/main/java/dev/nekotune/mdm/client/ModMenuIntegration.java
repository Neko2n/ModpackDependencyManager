package dev.nekotune.mdm.client;

import dev.nekotune.mdm.client.gui.ConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

@Environment(value = EnvType.CLIENT)
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
