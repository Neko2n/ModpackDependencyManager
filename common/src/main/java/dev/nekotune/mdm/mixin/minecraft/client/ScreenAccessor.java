package dev.nekotune.mdm.mixin.minecraft.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

@Mixin(value = Screen.class)
public interface ScreenAccessor {

    @Invoker(value = "addRenderableWidget")
    public <T extends GuiEventListener & Renderable & NarratableEntry> T mdm$invokeAddRenderableWidget(final T widget);
}
