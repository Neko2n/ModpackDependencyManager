package com.nekotune.mdm.mixin;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.nekotune.mdm.definition.DependencyInfo;

import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

@Mixin(PackSelectionModel.class)
public abstract class PackSelectionModelMixin {

    @Redirect(method = "findNewPacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;getAvailablePacks()Ljava/util/Collection;"))
    private Collection<Pack> mdm$hidePacks(final PackRepository repository) {
        return repository.getAvailablePacks().stream()
                .filter((final Pack pack) -> {
                    final Optional<DependencyInfo.Mode> mode = DependencyPackInfo.getMode(pack);
                    return mode.isPresent()
                            ? (!mode.orElseThrow().isHidden)
                            : true;
                })
                .collect(Collectors.toList());
    }
}
