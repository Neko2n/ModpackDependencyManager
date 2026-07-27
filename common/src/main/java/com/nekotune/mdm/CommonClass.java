package com.nekotune.mdm;

import com.nekotune.mdm.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;

public class CommonClass {

    public static void init() {
        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.get().getPlatformName(), Services.PLATFORM.get().getEnvironmentName());
        Constants.LOG.info("The ID for diamonds is {}", BuiltInRegistries.ITEM.getKey(Items.DIAMOND));

        if (Services.PLATFORM.get().isModLoaded("mdm")) {
            Constants.LOG.info("Hello to mdm");
        }
    }
}