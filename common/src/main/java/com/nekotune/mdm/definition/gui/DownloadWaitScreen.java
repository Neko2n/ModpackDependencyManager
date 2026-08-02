package com.nekotune.mdm.definition.gui;

import com.nekotune.mdm.Constants;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DownloadWaitScreen extends Screen {

    public static final Component TITLE = Component
            .translatable(Constants.MOD_ID + ".screen.downloadwait.title");

    private final Screen lastScreen;
    private final Runnable callback; // TODO: Fire callback once download is finished

    public DownloadWaitScreen(final Screen lastScreen, final Runnable callback) {
        super(TITLE);
        this.lastScreen = lastScreen;
        this.callback = callback;
    }

    @Override
    public void onClose() {
        Constants.LOG.debug("[DownloadWaitScreen] Screen closing");
        this.callback.run();
        this.minecraft.setScreen(lastScreen);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
