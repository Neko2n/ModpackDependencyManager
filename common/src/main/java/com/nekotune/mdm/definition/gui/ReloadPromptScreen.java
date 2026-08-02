package com.nekotune.mdm.definition.gui;

import com.nekotune.mdm.Constants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ReloadPromptScreen extends AlertScreen {

    private static final String ID = Constants.MOD_ID + ".screen.reloadprompt";

    public static final Component TITLE = Component
            .translatable(ID + ".title")
            .withStyle(ChatFormatting.BOLD);

    public static final Component MESSAGE = Component
            .translatable(ID + ".message");

    public static final Component BUTTON = Component
            .translatable(ID + ".button")
            .withStyle(ChatFormatting.BOLD);

    public ReloadPromptScreen(final Screen lastScreen, final Runnable callback) {
        super(() -> {
            Constants.LOG.debug("[ReloadPromptScreen] Reloading resources");
            callback.run();
            final Minecraft mc = Minecraft.getInstance();
            mc.setScreen(lastScreen);
            mc.reloadResourcePacks();
        }, TITLE, MESSAGE, BUTTON, false);
    }

    public ReloadPromptScreen(final Screen lastScreen) {
        this(lastScreen, () -> {
        });
    }

    @Override
    public void onClose() {
        Constants.LOG.debug("[ReloadPromptScreen] Screen closing");
    }
}
