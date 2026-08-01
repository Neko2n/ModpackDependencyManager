package com.nekotune.mdm;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.nekotune.mdm.DownloadManager.DownloadResult;
import com.nekotune.mdm.definition.gui.DownloadErrorScreen;
import com.nekotune.mdm.definition.gui.DownloadWaitScreen;
import com.nekotune.mdm.definition.gui.ReloadPromptScreen;
import com.nekotune.mdm.mixin.TitleScreenMixin;

import net.minecraft.client.Minecraft;

public class CommonClass {

    public static void init() {
        Constants.LOG.debug("[CommonClass] Hello from CommonClass#init");

        // Download dependencies
        DownloadThread.dispatch();
    }

    /**
     * @see TitleScreenMixin#mdm$onTitleScreenShown
     */
    public static void onTitleScreenShown(final Minecraft mc) {

        switch(DownloadThread.state) {
            case FINISHED:
                if (DownloadManager.getDownloaded().size() > 0) {

                    // Show invalid dependency errors
                    final Map<DownloadResult, List<String>> errors
                            = new EnumMap<>(DownloadResult.class);
                    for (final String cause : DownloadManager.DOWNLOAD_ERRORS.keySet()) {
                        final DownloadResult error = DownloadManager.DOWNLOAD_ERRORS.get(cause);
                        final List<String> causes = errors
                                .computeIfAbsent(error, $ -> new ArrayList<>());
                        causes.add(cause);
                    }
                    for (final DownloadResult error : errors.keySet()) {
                        Constants.LOG.debug("[CommonClass] Setting screen to DownloadError " + error.name());
                        mc.setScreen(new DownloadErrorScreen(mc.screen, error));
                    }

                    // Prompt the user to reload.
                    Constants.LOG.debug("[CommonClass] Setting screen to ReloadPrompt");
                    mc.setScreen(new ReloadPromptScreen(mc.screen));
                }
                break;
            case STARTED:
                
                // If dependencies are still downloading,
                // show the dependency download progress screen.
                Constants.LOG.debug("[CommonClass] Setting screen to DownloadWait");
                mc.setScreen(new DownloadWaitScreen(mc.screen));
                break;
            case FAILED:

                // If dependencies failed to download,
                // show the error screen.
                Constants.LOG.debug("[CommonClass] Setting screen to DownloadError IO_FAILURE");
                mc.setScreen(new DownloadErrorScreen(mc.screen, DownloadResult.IO_FAILURE));
                break;
            default:
                break;
        }
    }
}