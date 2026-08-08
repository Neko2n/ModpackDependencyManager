package dev.nekotune.mdm.client.gui.download;

import java.util.Optional;

import dev.nekotune.mdm.DownloadManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Overlay screen that displays when a user reaches the title screen before
 * all modpack web dependencies have finished downloading.
 */
public class DownloadWaitOverlay extends LoadingOverlay {

    private static final MutableComponent PROGRESS_TEXT = Component
            .translatableWithFallback("mdm.screen.downloadwait.progress",
                    "Downloading dependencies");

    private final DownloadReloadInstance reload;
    private final Minecraft minecraft;

    private DownloadWaitOverlay(final Minecraft mc, final DownloadReloadInstance reload,
            final boolean fadeIn) {
        super(mc, reload, DownloadWaitOverlay::onFinished, fadeIn);
        this.reload = reload;
        this.minecraft = mc;
    }

    public DownloadWaitOverlay(final Minecraft mc, final boolean fadeIn) {
        this(mc, new DownloadReloadInstance(DownloadManager.getTotal(),
                DownloadManager::getRemaining, DownloadManager.onDownloadsFinished), fadeIn);
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (this.reload.isDone())
            return;
        final int downloaded = this.reload.toDownload - this.reload.remaining.get();
        final int x = guiGraphics.guiWidth() / 2;
        final int f = (int)(guiGraphics.guiHeight() * (15f/64f));
        final int y = (guiGraphics.guiHeight() / 2) + f;
        final Component progressText = PROGRESS_TEXT.copy()
                .append(" (%d/%d)".formatted(downloaded, this.reload.toDownload))
                .withStyle(ChatFormatting.WHITE);
        guiGraphics.drawCenteredString(minecraft.font, progressText, x, y, 0xFFFFFFFF);
    }

    private static void onFinished(final Optional<Throwable> x) {
    }
}
