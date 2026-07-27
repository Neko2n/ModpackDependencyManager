package com.nekotune.mdm.config;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;
import com.nekotune.mdm.Constants;
import com.nekotune.mdm.lib.SimpleConfig.DefaultConfig;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.network.chat.Component;

public class FabricConfigProvider implements DefaultConfig {

    public static final ConfigBuilder CONFIG_BUILDER = ConfigBuilder.create()
            .setTitle(Component.literal(Constants.MOD_NAME + " Config"))
            .setSavingRunnable(() -> {
                // TODO: Persist changes
            });
    public static final class ConfigCategories {
        public static final ConfigCategory GENERAL = CONFIG_BUILDER
                .getOrCreateCategory(Component.literal("General"));
    }

    private String configContents = "";

    private final List<Pair<String, ?>> configsList = new ArrayList<>();

    public List<Pair<String, ?>> getConfigsList() { return configsList; }

    public void addKeyValuePair(Pair<String, ?> keyValuePair, String comment) {
        configsList.add(keyValuePair);
        configContents += keyValuePair.getFirst() + "=" + keyValuePair.getSecond() + " #"
                + comment + " | default: " + keyValuePair.getSecond() + "\n";
    }

    @Override
    public String get(String namespace) {
        return configContents;
    }
}
