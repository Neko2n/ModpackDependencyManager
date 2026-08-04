package com.nekotune.mdm.definition;

import java.io.IOException;
import java.util.Optional;

import com.nekotune.mdm.Config;
import com.nekotune.mdm.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;

/**
 * Custom Pack implementation for modpack dependencies.
 */
public class DependencyPack extends Pack {

    /**
     * Custom source type for modpack-loaded dependency packs.
     * Has a unique badge and color.
     */
    public static final PackSource PACK_SOURCE = PackSource.create((final Component component) -> {
        return Component.translatable("mdm.meta.pack-source",
                component, Component.literal("modpack"))
                .withStyle(ChatFormatting.BOLD)
                .withColor(Constants.TOKEN_COLOR);
    }, true);
    
    public final DependencyInfo info;

    protected DependencyPack(final DependencyInfo info, final PackLocationInfo location,
            final ResourcesSupplier resources, final Metadata metadata,
            final PackSelectionConfig selectionConfig) {
        super(location, resources, metadata, selectionConfig);
        this.info = info;
    }

    /**
     * Builds a dependency pack from the given dependency information.
     * 
     * @param info The information about the dependency to build the pack for.
     * @return The newly created DependencyPack object.
     * @throws IOException If the pack's file metadata could not be read.
     */
    public static DependencyPack from(final DependencyInfo info) throws IOException {
        final var location = new PackLocationInfo(info.packId(),
                Component.literal(info.fileName()), PACK_SOURCE,
                Optional.empty());
        final var supplier = new PathPackResources.PathResourcesSupplier(info.file());
        final int version = SharedConstants.getCurrentVersion().getPackVersion(info.type());
        final Metadata meta = Pack.readPackMetadata(location, supplier, version);
        if (meta == null) {
            throw new IOException("File metadata could not be read for dependency " + info.toString());
        }
        final boolean required = info.mode() == DependencyInfo.Mode.FORCED;
        final Position pos = required ? Position.BOTTOM : Position.TOP;
        final var selectionConfig = new PackSelectionConfig(required, pos, required);
        return new DependencyPack(info, location, supplier, meta, selectionConfig);
    }

    @Override
    public PackCompatibility getCompatibility() {
        return Config.INSTANCE.disableCompatibilityWarnings
                ? PackCompatibility.COMPATIBLE
                : super.getCompatibility();
    }
}
