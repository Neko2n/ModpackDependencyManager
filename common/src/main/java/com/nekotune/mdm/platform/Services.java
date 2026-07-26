package com.nekotune.mdm.platform;

import com.nekotune.mdm.Constants;
import com.nekotune.mdm.platform.services.IPlatformHelper;
import com.nekotune.mdm.platform.services.config.ConfigHelper;

import java.util.ServiceLoader;

public final class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final ConfigHelper CONFIG = load(ConfigHelper.class);

    private static ClassLoader loader;

    /**
     * Initializes the service loader to load from a specified class loader.
     * @param classLoader The class loader to use when loading services.
     */
    public static void init(final ClassLoader classLoader) {
        Services.loader = classLoader;
    }

    /**
     * Loads a platform-specific service class.
     * @param <T> The class type
     * @param clazz The class to load
     * @return The loaded service
     */
    private static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz, loader)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}