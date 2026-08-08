package dev.nekotune.mdm.client.gui.config;

import net.minecraft.network.chat.Component;

public class ResourcePacksScreen extends AbstractDependenciesScreen {

    protected static final String KEY = AbstractDependenciesScreen.KEY + ".resourcepacks";
    protected static final Component TITLE = Component.translatableWithFallback(KEY, "Dependencies: Resource Packs");

    public ResourcePacksScreen(final MainConfigScreen lastScreen) {
        super(TITLE, lastScreen);
    }
}
