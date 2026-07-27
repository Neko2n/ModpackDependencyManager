package com.nekotune.mdm.platform;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.datafixers.util.Pair;
import com.nekotune.mdm.Constants;
import com.nekotune.mdm.config.FabricConfigProvider;
import com.nekotune.mdm.lib.SimpleConfig;
import com.nekotune.mdm.platform.services.config.ConfigHelper;
import com.nekotune.mdm.platform.services.config.spec.ConfigDecorator;
import com.nekotune.mdm.platform.services.config.spec.ConfigValue;
import com.nekotune.mdm.platform.services.config.spec.IConfigEntry;

public class FabricConfigHelper extends ConfigHelper {

    private SimpleConfig config;
    private final FabricConfigProvider provider = new FabricConfigProvider();
    private final Map<ConfigValue<?>, Object> values = new HashMap<>();

    @Override
    public void register(final Path path) {
        createConfigs();
        config = SimpleConfig.of(Constants.CONFIG_FILE_NAME, path)
                .provider(provider)
                .request();
        assignConfigs();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T read(final ConfigValue<T> configValue) {
        return (T) values.get(configValue);
    }

    private void createConfigs() {
        Optional<String> nextComment = Optional.empty();
        for (final IConfigEntry configEntry : SPEC.entries()) {
            if (configEntry instanceof final ConfigValue<?> configValue) {
                final String comment = nextComment.orElse("");
                provider.addKeyValuePair(new Pair<>(configValue.path, configValue.defaultValue), comment);
            } else if (configEntry instanceof final ConfigDecorator configDecorator) {
                nextComment = Optional.of(configDecorator.comment);
            } else {
                throw new UnsupportedOperationException("Fabric config spec not implemented");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void assignConfigs() {
        for (final IConfigEntry configEntry : SPEC.entries()) {
            if (!(configEntry instanceof final ConfigValue<?> configValue))
                continue;
            if (configValue.defaultValue instanceof final Integer n) {
                values.put(configValue, config.getOrDefault(configValue.path, n));
            } else if (configValue.defaultValue instanceof final Float n) {
                values.put(configValue, config.getOrDefault(configValue.path, n));
            } else if (configValue.defaultValue instanceof final Double n) {
                values.put(configValue, config.getOrDefault(configValue.path, n));
            } else if (configValue.defaultValue instanceof final Boolean n) {
                values.put(configValue, config.getOrDefault(configValue.path, n));
            } else if (configValue.defaultValue instanceof final String n) {
                values.put(configValue, config.getOrDefault(configValue.path, n));
            } else if (configValue.defaultValue instanceof final List n) {
                values.put(configValue, config.getOrDefault(configValue.path, n));
            } else {
                throw new UnsupportedOperationException("Fabric config spec not implemented");
            }
        }
    }
}
