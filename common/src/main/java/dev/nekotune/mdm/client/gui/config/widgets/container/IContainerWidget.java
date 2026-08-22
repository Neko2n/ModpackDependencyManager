package dev.nekotune.mdm.client.gui.config.widgets.container;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;

public interface IContainerWidget<T extends Layout> {

    /**
     * @return The container widget's content, a layout of child widgets.
     */
    public abstract T getContent();
    
    /**
     * Updates the positions/arrangement of the container's content.
     */
    public abstract void updateContent();
    
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
        final AtomicBoolean handled = new AtomicBoolean(false);
        if (element instanceof final AbstractWidget widget)
            handled.set(handler.apply(widget));
        element.visitWidgets((final AbstractWidget child) -> {
            if (child == element)
                return;
            if (handleElementInteract(child, handler)) {
                handled.set(true);
            }
        });
        return handled.get();
    }
}
