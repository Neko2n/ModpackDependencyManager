package dev.nekotune.lace.mixin.minecraft.shared;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.BuiltInPackSource;

@Mixin(value = BuiltInPackSource.class)
public interface BuiltInPackSourceAccessor {

    @Accessor("packType")
    public PackType lace$getPackType();
}
