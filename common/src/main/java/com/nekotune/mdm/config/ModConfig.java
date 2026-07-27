package com.nekotune.mdm.config;

import com.nekotune.mdm.config.spec.CommonConfigSpec;
import com.nekotune.mdm.config.spec.ConfigEntry;

public final class ModConfig {
    public static final CommonConfigSpec SPEC;

    public static final ConfigEntry.FloatEntry TEST;
    
    static {
        final CommonConfigSpec.Builder builder = new CommonConfigSpec.Builder();

        TEST = builder.inRange("test",
                1f, 0f, 2f);
        TEST.fileComment = "Test Config Value (file comment)";

        SPEC = builder.build();
    }
}
