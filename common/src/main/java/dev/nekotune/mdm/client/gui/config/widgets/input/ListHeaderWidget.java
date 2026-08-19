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

    public static final int ARROW_SIZE = 20;
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

    public int contentMargin() {
        return ARROW_SIZE + 4;
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        final int x = this.getX();
        final int y = this.getY();
        guiGraphics.blitSprite(this.isCollapsed() ? ArrowSprites.CLOSED : ArrowSprites.OPEN,
                x, y + this.height / 2 - ARROW_SIZE / 2,
                ARROW_SIZE, ARROW_SIZE);
        guiGraphics.drawString(this.font, getMessage(),
                x + this.contentMargin(), y + this.height / 2 - this.font.lineHeight / 2,
                0xFFFFFFFF);
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
    }

    public static interface ArrowSprites {
        public static final ResourceLocation OPEN = ResourceLocation.fromNamespaceAndPath(
                Constants.MOD_ID, "icon/arrow_open");
        public static final ResourceLocation CLOSED = ResourceLocation.fromNamespaceAndPath(
                Constants.MOD_ID, "icon/arrow_closed");
    }
}