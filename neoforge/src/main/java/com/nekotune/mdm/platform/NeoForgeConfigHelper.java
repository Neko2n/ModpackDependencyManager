package com.nekotune.mdm.platform;

import com.nekotune.mdm.config.ModConfig;
import com.nekotune.mdm.config.spec.ConfigEntry;
import com.nekotune.mdm.platform.services.config.IConfigHelper;

import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoForgeConfigHelper implements IConfigHelper {

    public final ModConfigSpec loaderSpec;

    public NeoForgeConfigHelper() {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
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
