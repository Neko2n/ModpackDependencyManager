package com.nekotune.mdm.config.spec;

import java.util.LinkedList;
import java.util.List;

public final class CommonConfigSpec {

    private final List<ConfigEntry<?>> entries;
    
    private CommonConfigSpec(final List<ConfigEntry<?>> entries) {
        this.entries = entries;
    }

    public final List<ConfigEntry<?>> entries() {
        return List.copyOf(entries);
    }

    public static final class Builder {

        private final List<ConfigEntry<?>> entries = new LinkedList<>();

        public Builder() {}

        public ConfigEntry.IntegerEntry inRange(final String path,
                final int defaultValue, final int minValue, final int maxValue) {
            final var value = new ConfigEntry.IntegerEntry(path,
                    defaultValue, (final Object v) -> {
                        if (v instanceof final Integer n) {
                            return n >= minValue && n <= maxValue;
                        }
                        return false;
                    });
            entries.add(value);
            return value;
        }

        public ConfigEntry.FloatEntry inRange(final String path,
                final float defaultValue, final float minValue, final float maxValue) {
            final var value = new ConfigEntry.FloatEntry(path,
                    defaultValue, (final Object v) -> {
                        if (v instanceof final Float n) {
                            return n >= minValue && n <= maxValue;
                        }
                        return false;
                    });
            entries.add(value);
            return value;
        }

        public ConfigEntry.DoubleEntry inRange(final String path,
                final double defaultValue, final double minValue, final double maxValue) {
            final var value = new ConfigEntry.DoubleEntry(path,
                    defaultValue, (final Object v) -> {
                        if (v instanceof final Double n) {
                            return n >= minValue && n <= maxValue;
                        }
                        return false;
                    });
            entries.add(value);
            return value;
        }

        public ConfigEntry.BooleanEntry toggle(final String path, final boolean defaultValue) {
            final var value = new ConfigEntry.BooleanEntry(path, defaultValue);
            entries.add(value);
            return value;
        }

        // public FloatConfigEntry<List<String>> list(final String path,
        //         final List<String> defaultValue) {
        //     final FloatConfigEntry<List<String>> value = new FloatConfigEntry<>(path,
        //             defaultValue, (final Object v) -> v instanceof List);
        //     entries.add(value);
        //     return value;
        // }

        public CommonConfigSpec build() {
            return new CommonConfigSpec(entries);
        }
    }
}
