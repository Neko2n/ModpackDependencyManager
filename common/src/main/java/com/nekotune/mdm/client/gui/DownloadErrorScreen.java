package com.nekotune.mdm.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.nekotune.mdm.Constants;
import com.nekotune.mdm.DownloadManager.DownloadResult;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DownloadErrorScreen extends ErrorScreen {

    private static final String PATH = Constants.MOD_ID + ".screen.downloaderror";

    public static final Component TITLE = Component.translatable(PATH + ".title")
            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

    private final Screen lastScreen;
    public List<String> causes = new ArrayList<>();

    private DownloadErrorScreen(final Screen lastScreen, final Supplier<String> message) {
        super(TITLE, Component.translatable(PATH + ".message." + message.get()));
        this.lastScreen = lastScreen;
    }

    public DownloadErrorScreen(final Screen lastScreen, final DownloadResult errorType) {
        this(lastScreen, () -> {
            return errorType.toString().toLowerCase().replace('_', '-');
        });
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }
}
