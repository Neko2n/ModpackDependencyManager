package dev.nekotune.mdm.client.gui;

import dev.nekotune.mdm.Constants;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Screen that displays when a user reaches the title screen before dependencies
 * have finished downloading.
 */
public class DownloadWaitScreen extends Screen {

    public static final Component TITLE = Component
            .translatable(Constants.MOD_ID + ".screen.downloadwait.title");

    public final Screen covering;

    public DownloadWaitScreen(final Screen covering) {
        super(TITLE);
        this.covering = covering;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
