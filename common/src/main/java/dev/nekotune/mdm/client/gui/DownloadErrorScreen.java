package dev.nekotune.mdm.client.gui;

import java.util.List;
import java.util.Optional;

import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.DownloadManager.DownloadResult;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class DownloadErrorScreen extends Screen {

    private static final String PATH = Constants.MOD_ID + ".screen.downloaderror";

    public static final Component TITLE = Component.translatable(PATH + ".title")
            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

    private final Component message;
    private final Button.OnPress callback;
    private Optional<MultiLineTextWidget> messageWidget = Optional.empty();
    public final List<String> causes;

    private DownloadErrorScreen(final List<String> causes, final String message,
            final Button.OnPress callback) {
        super(TITLE);
        this.causes = causes;
        this.message = Component.translatable(PATH + ".message." + message + ".1")
                .append(Component.literal("\n"))
                .append(Component.translatable(PATH + ".message." + message + ".2"));
        this.callback = callback;
    }

    public DownloadErrorScreen(final DownloadResult errorType, final List<String> causes,
            final Button.OnPress callback) {
        this(causes, errorType.toString().toLowerCase().replace('_', '-'), callback);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CONTINUE, this.callback)
                .bounds(this.width / 2 - 100, 140, 200, 20).build());
        this.messageWidget = Optional.of(this.addRenderableWidget(
                new MultiLineTextWidget(this.message, this.font)
                        .setCentered(true)
                        .setColor(0xFFFFFFFF)));
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        final int yPos = this.height / 4;
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, yPos, 0xFFFFFFFF);
        this.messageWidget.ifPresent((final MultiLineTextWidget widget) -> {
            widget.setPosition(this.width / 2 - widget.getWidth() / 2, yPos + 20);
        });
    }

    @Override
    public void renderBackground(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -12574688, -11530224);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
