package com.nekotune.mdm.platform.services.config.spec;

import java.lang.constant.Constable;
import java.util.function.Predicate;

import com.nekotune.mdm.platform.Services;

public final class ConfigValue<T extends Constable> implements IConfigEntry {

    public final String path;
    public final T defaultValue;
    public final Predicate<Object> validator;

    public ConfigValue(final String path, final T defaultValue,
            final Predicate<Object> validator) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.validator = validator;
    }

    public T read() {
        return Services.CONFIG.read(this);
    }
}
