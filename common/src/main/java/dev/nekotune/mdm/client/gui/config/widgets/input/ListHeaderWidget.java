package dev.nekotune.mdm.client.gui.config.widgets.input;

import java.util.function.Supplier;

import dev.nekotune.mdm.Constants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ListHeaderWidget extends AbstractWidget {

    protected final Supplier<Boolean> collapsed;
    protected Font font;

    public ListHeaderWidget(final int x, final int y, final int width, final int height,
            final Component component, final Font font, final Supplier<Boolean> collapsed) {
        super(x, y, width, height, component);
        this.font = font;
        this.collapsed = collapsed;
    }

    public boolean isCollapsed() {
        return collapsed.get();
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        guiGraphics.blitSprite(this.isCollapsed() ? ArrowSprites.CLOSED : ArrowSprites.OPEN,
                0, 0, this.height, this.height);
        guiGraphics.drawString(font, getMessage(),
                this.height + 4, this.height / 2, 0xFFFFFFFF);
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
    }

    public static interface ArrowSprites {
        public static final ResourceLocation OPEN = ResourceLocation.fromNamespaceAndPath(
                Constants.MOD_ID, "widget/arrow_open");
        public static final ResourceLocation CLOSED = ResourceLocation.fromNamespaceAndPath(
                Constants.MOD_ID, "widget/arrow_closed");
    }
}