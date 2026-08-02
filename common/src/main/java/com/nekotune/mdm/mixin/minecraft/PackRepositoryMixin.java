package com.nekotune.mdm.mixin.minecraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.nekotune.mdm.Constants;
import com.nekotune.mdm.definition.DependencyInfo;
import com.nekotune.mdm.mixin.PackInfoAccessor;

import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    @Inject(method = "rebuildSelected", at = @At("RETURN"), cancellable = true)
    private void mdm$reorderPacks(final Collection<String> ids,
            final CallbackInfoReturnable<List<Pack>> cir) {
        Constants.LOG.debug("[PackRepositoryMixin] Ordering packs: " + ids.toString());
        final List<Pack> packs = cir.getReturnValue();
        cir.setReturnValue(applyPriorityOrder(packs));
    }

    private static List<Pack> applyPriorityOrder(final List<Pack> packs) {

        // Separate out all packs which were downloaded as dependencies
        final List<Pack> forcedPacks = new ArrayList<>();
        final List<Pack> supportPacks = new ArrayList<>();
        int n = 0;
        for (final Pack pack : packs) {
            final Optional<DependencyInfo.Mode> mode = PackInfoAccessor.getMode(pack);
            if (mode.isPresent()) {
                n++;
                switch (mode.orElseThrow()) {
                    case FORCED:
                        forcedPacks.add(pack);
                        break;
                    case SUPPORT:
                        supportPacks.add(pack);
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
        final Comparator<Pack> comparator = Comparator.comparingInt((final Pack pack) ->
                PackInfoAccessor.getLoadPriority(pack).orElseThrow());
        forcedPacks.sort(comparator);

        // Remove and re-insert forced packs after built-in packs
        result.removeAll(forcedPacks);
        int i = 0;
        while (i < result.size()) {
            Pack candidate = result.get(i);
            if (candidate.getPackSource() == PackSource.BUILT_IN
                    || (candidate.isRequired() && candidate.getPackSource() == PackSource.DEFAULT)) {
                i++;
                Constants.LOG.debug("[PackRepositoryMixin] Forced dependency insert shifted after pack \"" + candidate.getId() + "\" to position " + i);
            } else {
                break;
            }
        }
        result.addAll(i, forcedPacks);
        Constants.LOG.debug("[PackRepositoryMixin] Inserted " + forcedPacks.size() + " forced dependency packs at position " + i);
        
        return result;
    }
}
