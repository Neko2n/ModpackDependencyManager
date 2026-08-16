package dev.nekotune.mdm.client.gui.config.widgets.input;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.nekotune.mdm.client.gui.config.dependencies.ToggleSprites;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Simple input button representing a boolean setting.
 */
public abstract sealed class ToggleInput extends Button {

    protected boolean value;
    private Consumer<Boolean> responder = $ -> {};

    protected ToggleInput(final boolean defaultValue, final int width, final int height) {
        super(0, 0, width, height, Component.empty(), $ -> {}, Button.DEFAULT_NARRATION);
        this.value = defaultValue;
    }

    public boolean getValue() {
        return value;
    }

    public void setResponder(final Consumer<Boolean> responder) {
        this.responder = responder;
    }

    @Override
    public void onPress() {
        super.onPress();
        this.value = !this.value;
        responder.accept(this.value);
    }

    /**
     * A toggle button labelled by the component strings ON and OFF.
     */
    public static final class TextToggle extends ToggleInput {

        public TextToggle(final boolean defaultValue) {
            super(defaultValue, 60, Button.DEFAULT_HEIGHT);
            this.setMessage(getMessage());
        }

        @Override
        public void onPress() {
            super.onPress();
            this.setMessage(getMessage());
        }

        @Override
        public Component getMessage() {
            return Component.literal(this.value ? "ON" : "OFF");
        }
    }

    /**
     * A toggle button labelled by an icon.
     * The icon is greyed out when off.
     */
    public static final class IconToggle extends ToggleInput {

        private final ResourceLocation onSprite;
        private final ResourceLocation offSprite;

        public IconToggle(final ToggleSprites sprites, final boolean defaultValue) {
            super(defaultValue, Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT);
            this.onSprite = sprites.onSprite();
            this.offSprite = sprites.offSprite();
        }

        @Override
        public void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
                final float partialTick) {
            if (this.value) {
                RenderSystem.setShaderColor(0.7f, 0.7f, 0.75f, 1f);
            }
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            final ResourceLocation sprite = this.value ? this.onSprite : this.offSprite;
            guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
    }
}