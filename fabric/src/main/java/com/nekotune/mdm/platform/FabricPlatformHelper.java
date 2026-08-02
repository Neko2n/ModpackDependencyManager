package com.nekotune.mdm.platform;

import java.util.Optional;

import com.nekotune.mdm.platform.services.IPlatformHelper;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Dist dist() {
        switch (FabricLoader.getInstance().getEnvironmentType()) {
            case CLIENT:
                return Dist.CLIENT;
            case SERVER:
                return Dist.SERVER;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static Optional<MinecraftServer> server = Optional.empty();

    static {
        ServerTickEvents.START_SERVER_TICK.register(s -> {
            server = Optional.of(s);
        });
    }

    @Override
    public MinecraftServer getServer() {
        return server.orElseThrow();
    }
}
