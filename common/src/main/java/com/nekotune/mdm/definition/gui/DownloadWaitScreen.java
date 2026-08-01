package com.nekotune.mdm.definition.gui;

import com.nekotune.mdm.Constants;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DownloadWaitScreen extends Screen {

    public static final Component TITLE = Component.translatable(
            Constants.MOD_ID + ".screen.downloadwait.title");

    private final Screen lastScreen;

    public DownloadWaitScreen(final Screen lastScreen) {
        super(TITLE);
        this.lastScreen = lastScreen;
    }

    @Override
    public void onClose() {
        Constants.LOG.debug("[DownloadWaitScreen] Screen closing");
        this.minecraft.setScreen(lastScreen);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
