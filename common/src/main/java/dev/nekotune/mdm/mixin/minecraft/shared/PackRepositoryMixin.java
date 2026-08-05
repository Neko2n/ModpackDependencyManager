package dev.nekotune.mdm.mixin.minecraft.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.definition.DependencyPack;
import dev.nekotune.mdm.definition.DependencyPackSource;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    /**
     * Injects a dependency repository source whenever a pack repository
     * is initialized with a built-in source.
     */
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static RepositorySource[] mdm$injectDependencyPackSourceServer(final RepositorySource[] sources) {
        for (final RepositorySource source : sources) {
            if (source instanceof BuiltInPackSource) {
                final var accessor = (BuiltInPackSourceAccessor) source;
                final RepositorySource[] extended = Arrays.copyOf(sources, sources.length + 1);
                final PackType packType = accessor.mdm$getPackType();
                final RepositorySource added = new DependencyPackSource(packType);
                extended[sources.length] = added;
                return extended;
            }
        }
        return sources;
    }

    /**
     * Automatically enforce pack order of FORCED packs
     */
    @Inject(method = "rebuildSelected", at = @At("RETURN"), cancellable = true)
    private void mdm$reorderDependencies(final Collection<String> ids,
            final CallbackInfoReturnable<List<Pack>> cir) {
        final List<Pack> packs = cir.getReturnValue();
        cir.setReturnValue(reorderDependencies(packs));
    }

    private static List<Pack> reorderDependencies(final List<Pack> packs) {

        // Sort dependency packs into lists
        final List<DependencyPack> forced = new ArrayList<>();
        final List<DependencyPack> support = new ArrayList<>();
        for (final Pack pack : packs) {
            if (!(pack instanceof final DependencyPack dependency))
                continue;
            switch (dependency.info.mode()) {
                case FORCED:
                    forced.add(dependency);
                    break;
                case SUPPORT:
                    support.add(dependency);
                    break;
                default:
                    break;
            }
        }

        // Remove support packs
        final List<Pack> result = new ArrayList<>(packs);
        result.removeAll(support);

        // Sort forced packs by priority
        forced.sort(Comparator.comparingInt(
                (final DependencyPack pack) -> pack.info.loadPriority()));

        // Remove and re-insert forced packs after built-in packs
        result.removeAll(forced);
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
        result.addAll(i, forced);
        Constants.LOG.debug("[PackRepositoryMixin] Inserted " + forced.size()
                + " forced dependency packs at position " + i);

        return result;
    }
}
