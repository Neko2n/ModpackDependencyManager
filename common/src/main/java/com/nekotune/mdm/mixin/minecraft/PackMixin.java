package com.nekotune.mdm.mixin.minecraft;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.nekotune.mdm.Constants;
import com.nekotune.mdm.definition.DependencyInfo;
import com.nekotune.mdm.mixin.PackInfoAccessor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

@Mixin(Pack.class)
public abstract class PackMixin {

    private static final Component PACK_DECORATOR = Component
            .translatableWithFallback("mdm.meta.pack-decorator", " [MODPACK]")
            .withColor(Constants.TOKEN_COLOR)
            .withStyle(ChatFormatting.BOLD);

    // Override display title for modpack dependency packs
    @Inject(method = "getTitle", at = @At("HEAD"), cancellable = true)
    private void mdm$overrideTitle(final CallbackInfoReturnable<Component> ci) {
        final var pack = (Pack)(Object)this;
        final Optional<String> title = PackInfoAccessor.getTitle(pack);
        if (title.isPresent()) {
            ci.setReturnValue(Component.empty()
                    .append(title.orElseThrow())
                    .append(PACK_DECORATOR));
        }
    }

    // Enforce required for modpack dependency packs on mode FORCED
    @Inject(method = "isRequired", at = @At("HEAD"), cancellable = true)
    private void mdm$overrideIsRequired(final CallbackInfoReturnable<Boolean> ci) {
        final var pack = (Pack)(Object)this;
        final Optional<DependencyInfo.Mode> mode =
                PackInfoAccessor.getMode(pack);
        if (mode.isPresent() && mode.orElseThrow() == DependencyInfo.Mode.FORCED) {
            Constants.LOG.debug("[PackMixin] Set isRequired to TRUE for pack " + pack.getId());
            ci.setReturnValue(true);
        } else {
            Constants.LOG.debug("[PackMixin] Skipped override isRequired for pack " + pack.getId());
        }
    }

    // Enforce fixed position for hidden dependency packs
    @Inject(method = "isFixedPosition", at = @At("HEAD"), cancellable = true)
    private void mdm$overrideIsFixedPosition(final CallbackInfoReturnable<Boolean> ci) {
        final Optional<DependencyInfo.Mode> mode =
                PackInfoAccessor.getMode((Pack)(Object)this);
        if (mode.isPresent() && mode.orElseThrow().isHidden) {
            ci.setReturnValue(true);
        }
    }

    // Enforce FEATURE source for modpack dependency packs
    @Inject(method = "getPackSource", at = @At("HEAD"), cancellable = true)
    private void mdm$overridePackSource(final CallbackInfoReturnable<PackSource> ci) {
        final Optional<DependencyInfo.Mode> mode =
                PackInfoAccessor.getMode((Pack)(Object)this);
        if (mode.isPresent()) {
            ci.setReturnValue(PackSource.FEATURE);
        }
    }

    // Enforce bottom position for hidden dependency packs
    @Inject(method = "getDefaultPosition", at = @At("HEAD"), cancellable = true)
    private void mdm$forceBottomPosition(final CallbackInfoReturnable<Pack.Position> cir) {
        final Optional<DependencyInfo.Mode> mode =
                PackInfoAccessor.getMode((Pack)(Object)this);
        if (mode.isPresent() && mode.orElseThrow().isHidden) {
            cir.setReturnValue(Pack.Position.BOTTOM);
        }
    }
}
