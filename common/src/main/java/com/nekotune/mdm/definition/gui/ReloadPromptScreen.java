package com.nekotune.mdm.definition.gui;

import com.nekotune.mdm.Constants;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ReloadPromptScreen extends ConfirmScreen {

    private static final String ID = "reloadprompt";

    public static final Component TITLE = Component.translatable(
            Constants.MOD_ID + ".screen." + ID + ".title");

    public static final Component MESSAGE = Component.translatable(
            Constants.MOD_ID + ".screen." + ID + ".message");

    private final Screen lastScreen;

    public ReloadPromptScreen(final Screen lastScreen, final BooleanConsumer callback) {
        super(callback, TITLE, MESSAGE);
        this.lastScreen = lastScreen;
    }
    public ReloadPromptScreen(final Screen lastScreen) {
        this(lastScreen, b -> {});
    }

    @Override
    public void onClose() {
        Constants.LOG.debug("[ReloadPromptScreen] Screen closing");
        this.minecraft.reloadResourcePacks();
        this.minecraft.setScreen(lastScreen);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
