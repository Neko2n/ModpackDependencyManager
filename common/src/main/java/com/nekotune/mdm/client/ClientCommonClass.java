package com.nekotune.mdm.client;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.nekotune.mdm.CommonClass;
import com.nekotune.mdm.Config;
import com.nekotune.mdm.Constants;
import com.nekotune.mdm.DownloadManager;
import com.nekotune.mdm.DownloadManager.DownloadResult;
import com.nekotune.mdm.client.gui.DownloadErrorScreen;
import com.nekotune.mdm.client.gui.DownloadWaitScreen;
import com.nekotune.mdm.client.gui.ReloadPromptScreen;
import com.nekotune.mdm.platform.PlatformEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.packs.PackType;

public class ClientCommonClass {

    public static void init() {
        PlatformEvents.CLIENT_LOADED.hook.connect(ClientCommonClass::clientLoaded);

        // Enable OPTIONAL_ENABLED client packs by default
        DownloadManager.onDownloadFinished.connect(() -> {
            final Minecraft mc = Minecraft.getInstance();
            CommonClass.enableDownloadedOptionals(mc.getResourcePackRepository(), PackType.CLIENT_RESOURCES);
            final IntegratedServer server = mc.getSingleplayerServer();
            if (server != null) {
                CommonClass.enableDownloadedOptionals(server.getPackRepository(), PackType.SERVER_DATA);
            }
        });

        // If the download wait screen is showing when the download finishes,
        // change it to the reload prompt.
        final Minecraft mc = Minecraft.getInstance();
        DownloadManager.onDownloadFinished.connect(() -> {
            Constants.LOG.debug("[ClientCommonClass] onDownloadFinished called");
            mc.execute(() -> {
                if (mc.screen instanceof DownloadWaitScreen) {
                    mc.setScreen(null);
                    showReloadPrompt();
                }
            });
        });
    }

    /**
     * Called the first time the game reaches the title screen after loading.
     * Call site is loader-dependent.
     */
    public static void clientLoaded() {
        Constants.LOG.debug("[ClientCommonClass] clientLoaded called");
        final Minecraft mc = Minecraft.getInstance();

        final Runnable finished = () -> {
            if (DownloadManager.getDownloaded().size() == 0)
                return;

            // Show invalid dependency errors
            final Map<DownloadResult, List<String>> errors = new EnumMap<>(DownloadResult.class);
            for (final String cause : DownloadManager.DOWNLOAD_ERRORS.keySet()) {
                final DownloadResult error = DownloadManager.DOWNLOAD_ERRORS.get(cause);
                final List<String> causes = errors
                        .computeIfAbsent(error, $ -> new ArrayList<>());
                causes.add(cause);
            }
            for (final DownloadResult error : errors.keySet()) {
                Constants.LOG.debug("[ClientCommonClass] Setting screen to DownloadError " + error.name());
                mc.setScreen(new DownloadErrorScreen(mc.screen, error));
            }

            // Prompt the user to reload.
            showReloadPrompt();
        };

        mc.execute(() -> {
            switch (DownloadManager.getState()) {

                // Handle if any dependencies were successfully downloaded
                case FINISHED:
                    finished.run();
                    break;

                // If dependencies haven't finished downloading,
                // show the dependency download progress screen.
                case NOT_STARTED:
                case STARTED:
                    Constants.LOG.debug("[ClientCommonClass] Setting screen to DownloadWait");
                    mc.setScreen(new DownloadWaitScreen(mc.screen, () -> mc.execute(finished)));
                    break;

                // If the download thread was interrupted,
                // show the internal error screen.
                case INTERRUPTED:
                    Constants.LOG.debug("[ClientCommonClass] Setting screen to DownloadError EXCEPTION");
                    mc.setScreen(new DownloadErrorScreen(mc.screen, DownloadResult.EXCEPTION));
                    break;
            }
        });
    }

    private static void showReloadPrompt() {
        if (!Config.INSTANCE.promptEnabled)
            return;
        Constants.LOG.debug("[ClientCommonClass] Setting screen to ReloadPrompt");
        final Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new ReloadPromptScreen(mc.screen));
    }
}
