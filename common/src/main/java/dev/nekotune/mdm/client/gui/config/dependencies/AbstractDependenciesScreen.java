package dev.nekotune.mdm.client.gui.config.dependencies;

import java.util.Collection;
import java.util.LinkedHashSet;

import dev.nekotune.mdm.client.gui.config.AbstractConfigScreen;
import dev.nekotune.mdm.client.gui.config.widgets.ScrollListContent;
import dev.nekotune.mdm.definition.DependencyInfo;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class AbstractDependenciesScreen extends AbstractConfigScreen {

    protected static final String KEY = AbstractConfigScreen.KEY;

    private final LinkedHashSet<DependencyInfo> dependencies;

    protected AbstractDependenciesScreen(final Component title, final Screen lastScreen,
            final Collection<DependencyInfo> dependencies) {
        super(title, lastScreen);
        this.dependencies = new LinkedHashSet<>(dependencies);
    }

    protected abstract void apply(final LinkedHashSet<DependencyInfo> dependencies);

    @Override
    protected void buildScrollList(final ScrollListContent.Builder builder) {
        for (final DependencyInfo dependency : this.dependencies) {
            // TODO Dependency settings drop-down widget
        }

        // Button to add new dependencies to the list
        builder.addButton(KEY + ".button.add-new", (final Button button) -> {
            // TODO Add new dependency button
        });
    }

    @Override
    public void onClose() {
        this.apply(this.dependencies);
        super.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
