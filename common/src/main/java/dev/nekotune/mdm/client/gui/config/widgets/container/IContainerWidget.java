package dev.nekotune.mdm.client.gui.config.widgets.container;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;

public interface IContainerWidget {
    
    /**
     * Helper method to handle interaction detection for child widgets.
     * 
     * @param element The element to handle interaction detection for. Recursively
     *                handles its widgets.
     * @param handler The handler function to apply.
     * @return True if any widgets were handled, false otherwise.
     */
    public default boolean handleElementInteract(final LayoutElement element,
            final Function<AbstractWidget, Boolean> handler) {
        if (element instanceof final AbstractWidget widget)
            return handler.apply(widget);
        final AtomicBoolean handled = new AtomicBoolean(false);
        element.visitWidgets((final AbstractWidget child) -> {
            if (handleElementInteract(child, handler)) {
                handled.set(true);
            }
        });
        return handled.get();
    }
}
