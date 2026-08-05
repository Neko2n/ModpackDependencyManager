package dev.nekotune.mdm.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.DownloadManager.DownloadResult;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class DownloadErrorScreen extends Screen {

    private static final String PATH = Constants.MOD_ID + ".screen.downloaderror";

    public static final Component TITLE = Component.translatable(PATH + ".title")
            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

    private final Component message;
    private final Button.OnPress callback;
    public List<String> causes = new ArrayList<>();

    private DownloadErrorScreen(final Supplier<String> message, final Button.OnPress callback) {
        super(TITLE);
        this.message = Component.translatable(PATH + ".message." + message.get());
        this.callback = callback;
    }

    public DownloadErrorScreen(final DownloadResult errorType, final List<String> causes,
            final Button.OnPress callback) {
        this(() -> {
            return errorType.toString().toLowerCase().replace('_', '-');
        }, callback);
    }

    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CONTINUE, callback)
                .bounds(this.width / 2 - 100, 140, 200, 20).build());
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 90, 16777215);
        guiGraphics.drawCenteredString(this.font, this.message, this.width / 2, 110, 16777215);
    }

    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -12574688, -11530224);
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }
}
