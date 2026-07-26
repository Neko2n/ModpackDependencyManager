package com.nekotune.mdm.platform.services.config;

import java.lang.constant.Constable;

import com.nekotune.mdm.platform.services.config.spec.CommonConfigSpec;
import com.nekotune.mdm.platform.services.config.spec.ConfigValue;

public abstract class ConfigHelper {

    public static final CommonConfigSpec SPEC;

    public static final ConfigValue<Float> TEST;
    
    static {
        final CommonConfigSpec.Builder builder = new CommonConfigSpec.Builder();

        TEST = builder.inRange("test", 1f, 0f, 2f);

        SPEC = builder.build();
    }

    /**
     * @return Platform-specific accessor method for reading config values.
     */
    public abstract <T extends Constable> T read(final ConfigValue<T> configValue);
}
