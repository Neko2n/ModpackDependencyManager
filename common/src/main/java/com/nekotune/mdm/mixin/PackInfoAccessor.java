package com.nekotune.mdm.mixin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nekotune.mdm.Config;
import com.nekotune.mdm.definition.DependencyInfo;
import com.nekotune.mdm.mixin.minecraft.PackMixin;
import com.nekotune.mdm.mixin.minecraft.PackRepositoryMixin;

import net.minecraft.server.packs.repository.Pack;

/**
 * @see PackMixin
 * @see PackRepositoryMixin
 * @see PackSelectionModelMixin
 */
public final class PackInfoAccessor {
    
    private static final Map<String, DependencyInfo> SETTINGS_CACHE = new HashMap<>();

    private static Optional<DependencyInfo> getSettings(final String id) {
        if (SETTINGS_CACHE.containsKey(id))
            return Optional.of(SETTINGS_CACHE.get(id));
        final List<DependencyInfo> candidates = Config.INSTANCE.dependencies
                .stream()
                .filter(t -> id.equalsIgnoreCase("file/modpack." + t.slug() + ".zip")).toList();
        if (candidates.isEmpty())
            return Optional.empty();
        final DependencyInfo settings = candidates.get(0);
        SETTINGS_CACHE.put(id, settings);
        return Optional.of(settings);
    }

    public static Optional<DependencyInfo.Mode> getMode(final Pack pack) {
        final String id = pack.location().id().toLowerCase();
        final Optional<DependencyInfo> settings = getSettings(id);
        if (settings.isEmpty())
            return Optional.empty();
        return Optional.of(settings.orElseThrow().mode());
    }

    public static Optional<Integer> getLoadPriority(final Pack pack) {
        final String id = pack.location().id().toLowerCase();
        final Optional<DependencyInfo> settings = getSettings(id);
        if (settings.isEmpty())
            return Optional.empty();
        return Optional.of(settings.orElseThrow().loadPriority());
    }

    public static Optional<String> getTitle(final Pack pack) {
        final String id = pack.location().id().toLowerCase();
        final Optional<DependencyInfo> settings = getSettings(id);
        if (settings.isEmpty())
            return Optional.empty();
        return Optional.of(settings.orElseThrow().title());
    }
}
