package com.nekotune.mdm.platform.services.config;

import com.nekotune.mdm.config.spec.ConfigEntry;

public interface IConfigHelper {

    /**
     * @return Platform-specific accessor method for reading config values.
     */
    public abstract <T> T read(final ConfigEntry<T> configValue);
}
