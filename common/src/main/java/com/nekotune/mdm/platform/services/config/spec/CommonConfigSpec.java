package com.nekotune.mdm.platform.services.config.spec;

import java.util.LinkedList;
import java.util.List;

public final class CommonConfigSpec {

    private final List<IConfigEntry> entries;
    
    private CommonConfigSpec(final List<IConfigEntry> entries) {
        this.entries = entries;
    }

    public final List<IConfigEntry> entries() {
        return List.copyOf(entries);
    }

    public static final class Builder {

        private final List<IConfigEntry> entries = new LinkedList<>();

        public Builder() {}

        public Builder comment(final String comment) {
            entries.add(ConfigDecorator.comment(comment));
            return this;
        }

        public ConfigValue<Integer> inRange(final String path,
                final int defaultValue, final int minValue, final int maxValue) {
            final ConfigValue<Integer> value = new ConfigValue<>(path,
                    defaultValue, (final Object v) -> {
                        if (v instanceof final Integer n) {
                            return n >= minValue && n <= maxValue;
                        }
                        return false;
                    });
            entries.add(value);
            return value;
        }

        public ConfigValue<Float> inRange(final String path,
                final float defaultValue, final float minValue, final float maxValue) {
            final ConfigValue<Float> value = new ConfigValue<>(path,
                    defaultValue, (final Object v) -> {
                        if (v instanceof final Float n) {
                            return n >= minValue && n <= maxValue;
                        }
                        return false;
                    });
            entries.add(value);
            return value;
        }

        public ConfigValue<Double> inRange(final String path,
                final double defaultValue, final double minValue, final double maxValue) {
            final ConfigValue<Double> value = new ConfigValue<>(path,
                    defaultValue, (final Object v) -> {
                        if (v instanceof final Double n) {
                            return n >= minValue && n <= maxValue;
                        }
                        return false;
                    });
            entries.add(value);
            return value;
        }

        public ConfigValue<Boolean> toggle(final String path, final boolean defaultValue) {
            final ConfigValue<Boolean> value = new ConfigValue<>(path,
                    defaultValue, (final Object v) -> {
                        return v instanceof Boolean;
                    });
            entries.add(value);
            return value;
        }

        public CommonConfigSpec build() {
            return new CommonConfigSpec(entries);
        }
    }
}
