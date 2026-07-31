package com.nekotune.mdm;

import java.nio.file.Path;

public final class ConfigHandler {

    /**
     * Where the configuration file is located.
     */
    public static final Path FILE_PATH = Path.of("config",
            "dependencies." + Constants.MOD_ID + ".json");

    public static void init() {
    }
}
