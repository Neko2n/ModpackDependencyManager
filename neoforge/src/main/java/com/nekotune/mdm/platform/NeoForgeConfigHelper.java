package com.nekotune.mdm.platform;

import java.lang.constant.Constable;

import com.nekotune.mdm.platform.services.config.ConfigHelper;
import com.nekotune.mdm.platform.services.config.spec.ConfigDecorator;
import com.nekotune.mdm.platform.services.config.spec.ConfigValue;
import com.nekotune.mdm.platform.services.config.spec.IConfigEntry;

import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoForgeConfigHelper extends ConfigHelper {

    public static final ModConfigSpec NEOFORGE_SPEC;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SPEC.entries().forEach((final IConfigEntry configEntry) -> {
            if (configEntry instanceof final ConfigValue<?> configValue) {
                builder.define(configValue.path, configValue.defaultValue, configValue.validator);
            } else if (configEntry instanceof final ConfigDecorator configDecorator) {
                builder.comment(configDecorator.comment);
            } else {
                throw new UnsupportedOperationException("NeoForge config spec not implemented");
            }
        });
        NEOFORGE_SPEC = builder.build();
    }

    @Override
    public <T extends Constable> T read(ConfigValue<T> configValue) {
        return NEOFORGE_SPEC.getValues().get(configValue.path);
    }
}
