package com.nekotune.mdm.definition;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;
import com.nekotune.mdm.Config;
import com.nekotune.mdm.Constants;
import com.nekotune.mdm.mixin.minecraft.shared.PackRepositoryMixin;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;

/**
 * Repository source type which loads dependency packs.
 * 
 * @see PackRepositoryMixin
 */
public class DependencyPackSource implements RepositorySource {

    private final PackType packType;

    public DependencyPackSource(final PackType packType) {
        this.packType = packType;
    }

    @Override
    public void loadPacks(final Consumer<Pack> onLoad) {
        for (final DependencyInfo dependency : Config.INSTANCE.dependencies) {
            if (dependency.type() != this.packType)
                continue;
            if (!Files.exists(dependency.packDir()))
                continue;
            try {
                final DependencyPack pack = DependencyPack.from(dependency);
                onLoad.accept(pack);
            } catch (final IOException e) {
                Constants.LOG.error("Failed to load pack for dependency: " + dependency, e);
            }
        }
    }
}
