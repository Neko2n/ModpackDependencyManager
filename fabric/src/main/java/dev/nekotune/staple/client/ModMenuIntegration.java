package dev.nekotune.staple.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.nekotune.staple.client.gui.config.MainConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

@Environment(value = EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<MainConfigScreen> getModConfigScreenFactory() {
        return new ConfigScreenFactory<>(){

            @Override
            public MainConfigScreen create(final Screen lastScreen) {
                return new MainConfigScreen(lastScreen);
            }
        };
    }
}
