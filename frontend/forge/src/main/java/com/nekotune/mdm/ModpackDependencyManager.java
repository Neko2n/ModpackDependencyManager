package com.nekotune.mdm;

import com.nekotune.mdm.platform.Services;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class ModpackDependencyManager {

    public ModpackDependencyManager(final FMLJavaModLoadingContext context) {
        Services.init(this.getClass().getClassLoader());
        CommonClass.init();
    }
}