package dev.nekotune.mdm.client.gui.config;

import net.minecraft.network.chat.Component;

public class DataPacksScreen extends AbstractDependenciesScreen {

    protected static final String KEY = AbstractDependenciesScreen.KEY + ".datapacks";
    protected static final Component TITLE = Component.translatableWithFallback(KEY, "Dependencies: Data Packs");
    
    public DataPacksScreen(final MainConfigScreen lastScreen) {
        super(TITLE, lastScreen);
    }
}
