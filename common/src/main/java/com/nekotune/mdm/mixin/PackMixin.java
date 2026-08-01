package com.nekotune.mdm.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.nekotune.mdm.definition.DependencyInfo;

import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

@Mixin(Pack.class)
public abstract class PackMixin {

    @Inject(method = "isRequired", at = @At("HEAD"), cancellable = true)
    private void mdm$overrideIsRequired(final CallbackInfoReturnable<Boolean> ci) {
        final Optional<DependencyInfo.Mode> mode =
                DependencyPackInfo.getMode((Pack)(Object)this);
        if (mode.isPresent()) {
            switch (mode.orElseThrow()) {
                case FORCED:
                    ci.setReturnValue(true);
                    break;
                case SUPPORT:
                    ci.setReturnValue(false);
                    break;
                default:
                    break;
            }
        }
    }

    @Inject(method = "isFixedPosition", at = @At("HEAD"), cancellable = true)
    private void mdm$overrideIsFixedPosition(final CallbackInfoReturnable<Boolean> ci) {
        final Optional<DependencyInfo.Mode> mode =
                DependencyPackInfo.getMode((Pack)(Object)this);
        if (mode.isPresent() && mode.orElseThrow().isHidden) {
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "getPackSource", at = @At("HEAD"), cancellable = true)
    private void mdm$overridePackSource(final CallbackInfoReturnable<PackSource> ci) {
        final Optional<DependencyInfo.Mode> mode =
                DependencyPackInfo.getMode((Pack)(Object)this);
        if (mode.isPresent() && mode.orElseThrow().isHidden) {
            ci.setReturnValue(PackSource.FEATURE);
        }
    }

    @Inject(method = "isFixedPosition", at = @At("HEAD"), cancellable = true)
    private void mdm$forceFixedPosition(CallbackInfoReturnable<Boolean> cir) {
        final Optional<DependencyInfo.Mode> mode =
                DependencyPackInfo.getMode((Pack)(Object)this);
        if (mode.isPresent() && mode.orElseThrow().isHidden) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getDefaultPosition", at = @At("HEAD"), cancellable = true)
    private void mdm$forceBottomPosition(CallbackInfoReturnable<Pack.Position> cir) {
        final Optional<DependencyInfo.Mode> mode =
                DependencyPackInfo.getMode((Pack)(Object)this);
        if (mode.isPresent() && mode.orElseThrow().isHidden) {
            cir.setReturnValue(Pack.Position.BOTTOM);
        }
    }
}
