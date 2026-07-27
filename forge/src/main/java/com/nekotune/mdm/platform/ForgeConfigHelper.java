package com.nekotune.mdm.platform;

import com.nekotune.mdm.config.ModConfig;
import com.nekotune.mdm.config.spec.ConfigEntry;
import com.nekotune.mdm.platform.services.config.IConfigHelper;

import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeConfigHelper implements IConfigHelper {

    public final ForgeConfigSpec loaderSpec;

    public ForgeConfigHelper() {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        for (final ConfigEntry<?> entry : ModConfig.SPEC.entries()) {
            builder.comment(entry.fileComment)
                    .translation(entry.translationKey())
                    .define(entry.path, entry.defaultValue, entry.validator);
        }
        loaderSpec = builder.build();
    }
    
    @Override
    public <T> T read(ConfigEntry<T> configValue) {
        return loaderSpec.getValues().get(configValue.path);
    }
}
