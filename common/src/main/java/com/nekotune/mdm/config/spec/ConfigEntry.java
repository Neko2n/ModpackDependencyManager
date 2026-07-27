package com.nekotune.mdm.config.spec;

import java.util.function.Predicate;

import com.nekotune.mdm.Constants;
import com.nekotune.mdm.platform.Services;

public sealed abstract class ConfigEntry<T> {

    public final String path;
    public final T defaultValue;
    public final Predicate<Object> validator;
    public String fileComment = "";

    public ConfigEntry(final String path, final T defaultValue,
            final Predicate<Object> validator) {
        this.path = path.toLowerCase().strip();
        this.defaultValue = defaultValue;
        this.validator = validator;
    }

    public String translationKey() {
        return Constants.MOD_ID + ".config." + path;
    }

    public T read() {
        return Services.CONFIG.get().read(this);
    }

    public static final class IntegerEntry extends ConfigEntry<Integer> {
        public IntegerEntry(final String path, final int defaultValue,
                final Predicate<Object> validator) {
            super(path, defaultValue, validator);
        }
    }

    public static final class FloatEntry extends ConfigEntry<Float> {
        public FloatEntry(final String path, final float defaultValue,
                final Predicate<Object> validator) {
            super(path, defaultValue, validator);
        }
    }

    public static final class DoubleEntry extends ConfigEntry<Double> {
        public DoubleEntry(final String path, final double defaultValue,
                final Predicate<Object> validator) {
            super(path, defaultValue, validator);
        }
    }

    public static final class BooleanEntry extends ConfigEntry<Boolean> {
        public BooleanEntry(final String path, final boolean defaultValue) {
            super(path, defaultValue, v -> v instanceof Boolean);
        }
    }
}
