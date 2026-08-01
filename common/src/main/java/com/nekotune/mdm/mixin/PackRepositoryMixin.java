package com.nekotune.mdm.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    @Inject(method = "rebuildSelected", at = @At("RETURN"), cancellable = true)
    private void mdm$reorderPacks(final Collection<String> ids,
            final CallbackInfoReturnable<List<Pack>> cir) {
        cir.setReturnValue(applyPriorityOrder(cir.getReturnValue()));
    }

    private static List<Pack> applyPriorityOrder(final List<Pack> packs) {

        // Separate out all packs which were downloaded as dependencies
        final List<Pack> dependencyPacks = new ArrayList<>();
        for (final Pack pack : packs) {
            if (DependencyPackInfo.getLoadPriority(pack).isPresent()) {
                dependencyPacks.add(pack);
            }
        }
        if (dependencyPacks.isEmpty()) {
            return packs;
        }

        // Sort pack order by priority
        dependencyPacks.sort(Comparator.comparingInt((final Pack pack) ->
                DependencyPackInfo.getLoadPriority(pack).orElseThrow()));

        // Remove and re-insert after built-in packs (mod resources, vanilla assets)
        final List<Pack> result = new ArrayList<>(packs);
        result.removeAll(dependencyPacks);
        int i = 0;
        while (i < result.size()) {
            Pack candidate = result.get(i);
            if (candidate.getPackSource() == PackSource.BUILT_IN) {
                i++;
            } else {
                break;
            }
        }
        result.addAll(i, dependencyPacks);
        return result;
    }
}
