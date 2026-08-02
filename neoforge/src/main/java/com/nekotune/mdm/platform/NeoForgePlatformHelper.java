package com.nekotune.mdm.platform;

import com.nekotune.mdm.platform.services.IPlatformHelper;

import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public Dist dist() {
        switch (FMLEnvironment.dist) {
            case CLIENT:
                return Dist.CLIENT;
            case DEDICATED_SERVER:
                return Dist.SERVER;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}