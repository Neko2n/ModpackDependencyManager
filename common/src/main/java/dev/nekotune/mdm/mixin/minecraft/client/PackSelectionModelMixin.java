package dev.nekotune.mdm.mixin.minecraft.client;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.nekotune.mdm.Config;
import dev.nekotune.mdm.definition.DependencyInfo;
import dev.nekotune.mdm.definition.DependencyPack;
import dev.nekotune.mdm.definition.DependencyPack.ModpackResources;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

@Mixin(PackSelectionModel.class)
public abstract class PackSelectionModelMixin {

    // Hide hidden packs 
    @Redirect(method = "findNewPacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;getAvailablePacks()Ljava/util/Collection;"))
    private Collection<Pack> mdm$getVisiblePacks(final PackRepository repository) {
        return repository.getAvailablePacks().stream()
                .filter(p -> isVisible(repository, p))
                .toList();
    }

    private static boolean isVisible(final PackRepository repository, final Pack pack) {

        // Hide modpack resources
        if (pack instanceof ModpackResources)
            return false;

        // Hide hidden dependency packs depending on configuration settings
        if (pack instanceof final DependencyPack dependency) {
            if (dependency.info.mode() == DependencyInfo.Mode.FORCED) {
                return !Config.INSTANCE.hideForced;
            }
            return !dependency.info.mode().isHidden;
        }

        // Use vanilla logic on non-dependency packs
        return repository.isAvailable(pack.getId());
    }
}
