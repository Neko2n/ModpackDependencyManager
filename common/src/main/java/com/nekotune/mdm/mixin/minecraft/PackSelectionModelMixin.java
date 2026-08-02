package com.nekotune.mdm.mixin.minecraft;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.nekotune.mdm.Config;
import com.nekotune.mdm.definition.DependencyInfo;
import com.nekotune.mdm.mixin.PackInfoAccessor;

import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

@Mixin(PackSelectionModel.class)
public abstract class PackSelectionModelMixin {

    // Hide hidden packs when in a production environment
    @Redirect(method = "findNewPacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;getAvailablePacks()Ljava/util/Collection;"))
    private Collection<Pack> mdm$getVisiblePacks(final PackRepository repository) {
        return repository.getAvailablePacks().stream()
                .filter(p -> isVisible(repository, p))
                .collect(Collectors.toList());
    }

    private static boolean isVisible(final PackRepository repository, final Pack pack) {
        final Optional<DependencyInfo.Mode> modeRef = PackInfoAccessor.getMode(pack);

        // Use vanilla logic on non-dependency packs
        if (!modeRef.isPresent()) {
            return repository.isAvailable(pack.getId());
        }

        // Hide hidden dependency packs depending on configuration settings
        final DependencyInfo.Mode mode = modeRef.orElseThrow();
        if (mode == DependencyInfo.Mode.FORCED) {
            return !Config.INSTANCE.hideForced;
        }
        return !mode.isHidden;
    }
}
