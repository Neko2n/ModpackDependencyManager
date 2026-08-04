package com.nekotune.mdm.mixin.minecraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.nekotune.mdm.Constants;
import com.nekotune.mdm.definition.DependencyPack;

import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;

/**
 * Modify pack repositories to automatically enforce modpack
 * dependency loading order.
 */
@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    @Inject(method = "rebuildSelected", at = @At("RETURN"), cancellable = true)
    private void mdm$injectDependencies(final Collection<String> ids,
            final CallbackInfoReturnable<List<Pack>> cir) {
        Constants.LOG.debug("[PackRepositoryMixin] Ordering packs: " + ids.toString());
        final List<Pack> packs = cir.getReturnValue();
        cir.setReturnValue(injectDependencies(packs));
    }

    private static List<Pack> injectDependencies(final List<Pack> packs) {

        // Sort dependency packs
        final List<DependencyPack> forcedPacks = new ArrayList<>();
        final List<DependencyPack> supportPacks = new ArrayList<>();
        int n = 0;
        for (final Pack pack : packs) {
            if (pack instanceof final DependencyPack dependency) {
                n++;
                switch (dependency.info.mode()) {
                    case FORCED:
                        forcedPacks.add(dependency);
                        break;
                    case SUPPORT:
                        supportPacks.add(dependency);
                        break;
                    default:
                        break;
                }
            }
        }
        if (n == 0) {
            return packs;
        }

        // Remove support packs
        final List<Pack> result = new ArrayList<>(packs);
        result.removeAll(supportPacks);

        // Sort forced packs by priority
        forcedPacks.sort(Comparator.comparingInt(
                (final DependencyPack pack) -> pack.info.loadPriority()));

        // Remove and re-insert forced packs after built-in packs
        result.removeAll(forcedPacks);
        int i = 0;
        while (i < result.size()) {
            final Pack candidate = result.get(i);
            if (candidate.getPackSource() == PackSource.BUILT_IN
                    || (candidate.isRequired() && candidate.getPackSource() == PackSource.DEFAULT)) {
                i++;
                Constants.LOG.debug("[PackRepositoryMixin] Forced dependency insert shifted after pack \""
                        + candidate.getId() + "\" to position " + i);
            } else {
                break;
            }
        }
        result.addAll(i, forcedPacks);
        Constants.LOG.debug(
                "[PackRepositoryMixin] Inserted " + forcedPacks.size() + " forced dependency packs at position " + i);

        return result;
    }
}
