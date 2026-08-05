package dev.nekotune.mdm.platform;

import dev.nekotune.mdm.platform.services.IPlatformHelper;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;

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
}