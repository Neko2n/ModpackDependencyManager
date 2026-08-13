package dev.nekotune.mdm.client.gui.config.dependencies;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import dev.nekotune.mdm.Config;
import dev.nekotune.mdm.definition.DependencyInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;

public class ResourcePacksScreen extends AbstractDependenciesScreen {

    protected static final String KEY = AbstractDependenciesScreen.KEY + ".resource-packs";
    protected static final Component TITLE = Component
            .translatableWithFallback(KEY + ".title", "Resource Packs")
            .withStyle(ChatFormatting.BOLD);

    public ResourcePacksScreen(final Screen lastScreen) {
        super(TITLE, lastScreen,
                Config.INSTANCE.dependencies.stream()
                        .filter(d -> d.type() == PackType.CLIENT_RESOURCES)
                        .toList());
    }

    @Override
    protected void apply(final LinkedHashSet<DependencyInfo> resourcePacks) {
        Config.INSTANCE.dependencies = new ArrayList<>(Config.INSTANCE.dependencies.stream()
                .filter((final DependencyInfo dependency) -> dependency.type() != PackType.CLIENT_RESOURCES)
                .toList());
        Config.INSTANCE.dependencies.addAll(resourcePacks);
    }
}
