package com.nekotune.mdm.mixin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nekotune.mdm.Config;
import com.nekotune.mdm.Config.DependencySettings;
import com.nekotune.mdm.definition.DependencyInfo;

import net.minecraft.server.packs.repository.Pack;

public final class DependencyPackInfo {
    
    private static final Map<String, DependencySettings> SETTINGS_CACHE = new HashMap<>();

    private static Optional<DependencySettings> getSettings(final String id) {
        if (SETTINGS_CACHE.containsKey(id))
            return Optional.of(SETTINGS_CACHE.get(id));
        final List<DependencySettings> candidates = Config.INSTANCE.dependencies
                .stream()
                .filter(t -> id == t.slug)
                .toList();
        if (candidates.isEmpty())
            return Optional.empty();
        final DependencySettings settings = candidates.get(0);
        SETTINGS_CACHE.put(id, settings);
        return Optional.of(settings);
    }

    public static Optional<DependencyInfo.Mode> getMode(final Pack pack) {
        final String id = pack.location().id().toLowerCase();
        final Optional<DependencySettings> settings = getSettings(id);
        if (settings.isEmpty())
            return Optional.empty();
        return Optional.of(settings.orElseThrow().mode);
    }

    public static Optional<Integer> getLoadPriority(final Pack pack) {
        final String id = pack.location().id().toLowerCase();
        final Optional<DependencySettings> settings = getSettings(id);
        if (settings.isEmpty())
            return Optional.empty();
        return Optional.of(settings.orElseThrow().loadPriority);
    }


}
