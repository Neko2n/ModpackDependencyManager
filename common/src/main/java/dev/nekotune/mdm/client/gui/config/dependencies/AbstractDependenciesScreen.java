package dev.nekotune.mdm.client.gui.config.dependencies;

import dev.nekotune.mdm.client.gui.config.AbstractConfigScreen;
import dev.nekotune.mdm.client.gui.config.MainConfigScreen;
import net.minecraft.network.chat.Component;

public abstract class AbstractDependenciesScreen extends AbstractConfigScreen {

    protected static final String KEY = AbstractConfigScreen.Components.KEY + ".dependencies";

    public AbstractDependenciesScreen(final Component title, final MainConfigScreen lastScreen) {
        super(title, lastScreen);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
