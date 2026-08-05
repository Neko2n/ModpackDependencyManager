package dev.nekotune.mdm.client.gui;

import dev.nekotune.mdm.Constants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
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

    public ReloadPromptScreen(final Runnable callback) {
        super(() -> {
            Constants.LOG.debug("[ReloadPromptScreen] Reloading resources");
            callback.run();
            Minecraft.getInstance().reloadResourcePacks();
        }, TITLE, MESSAGE, BUTTON, false);
    }

    public ReloadPromptScreen() {
        this(() -> {
        });
    }
}
