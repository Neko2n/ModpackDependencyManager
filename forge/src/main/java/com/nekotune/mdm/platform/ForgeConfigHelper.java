package com.nekotune.mdm.platform;

import java.lang.constant.Constable;

import com.nekotune.mdm.platform.services.config.ConfigHelper;
import com.nekotune.mdm.platform.services.config.spec.ConfigDecorator;
import com.nekotune.mdm.platform.services.config.spec.ConfigValue;
import com.nekotune.mdm.platform.services.config.spec.IConfigEntry;

import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeConfigHelper extends ConfigHelper {

    public static final ForgeConfigSpec FORGE_SPEC;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SPEC.entries().forEach((final IConfigEntry configEntry) -> {
            if (configEntry instanceof final ConfigValue<?> configValue) {
                builder.define(configValue.path, configValue.defaultValue, configValue.validator);
            } else if (configEntry instanceof final ConfigDecorator configDecorator) {
                builder.comment(configDecorator.comment);
            } else {
                throw new UnsupportedOperationException("Forge config spec not implemented");
            }
        });
        FORGE_SPEC = builder.build();
    }
    
    @Override
    public <T extends Constable> T read(ConfigValue<T> configValue) {
        return FORGE_SPEC.getValues().get(configValue.path);
    }
}
