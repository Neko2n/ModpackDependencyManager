package com.nekotune.mdm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.nekotune.mdm.DownloadManager.DownloadResult;
import com.nekotune.mdm.definition.DependencyInfo;
import com.nekotune.mdm.definition.gui.DownloadErrorScreen;
import com.nekotune.mdm.definition.gui.DownloadWaitScreen;
import com.nekotune.mdm.definition.gui.ReloadPromptScreen;
import com.nekotune.mdm.mixin.minecraft.TitleScreenMixin;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

public final class CommonClass {

    public static void init() {
        // Load configuration settings from file
        Config.INSTANCE.load();

        // Download dependencies
        final CompletableFuture<Void> download = DownloadTaskThread.start();
        download.thenRunAsync(() -> {
            final int downloaded = DownloadManager.getDownloaded().size();
            Constants.LOG.debug("[CommonClass] Download finished; " + downloaded + " files downloaded");
            if (downloaded > 0) {
                onDownloadFinished();
            }
        }, Minecraft.getInstance());
    }

    /**
     * Called the first time the game reaches the title screen after loading.
     * 
     * @see TitleScreenMixin#mdm$onTitleScreen
     */
    public static void gameLoadingFinished(final Minecraft mc) {

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
                Constants.LOG.debug("[CommonClass] Setting screen to DownloadError " + error.name());
                mc.setScreen(new DownloadErrorScreen(mc.screen, error));
            }

            // Prompt the user to reload.
            Constants.LOG.debug("[CommonClass] Setting screen to ReloadPrompt");
            mc.setScreen(new ReloadPromptScreen(mc.screen));
        };

        switch (DownloadTaskThread.state) {

            // Handle if any dependencies were successfully downloaded
            case FINISHED:
                finished.run();
                break;

            // If dependencies are still downloading,
            // show the dependency download progress screen.
            case STARTED:
                Constants.LOG.debug("[CommonClass] Setting screen to DownloadWait");
                mc.setScreen(new DownloadWaitScreen(mc.screen, finished));
                break;
                
            // If the download thread was interrupted,
            // show the internal error screen.
            case INTERRUPTED:
                Constants.LOG.debug("[CommonClass] Setting screen to DownloadError IO_FAILURE");
                mc.setScreen(new DownloadErrorScreen(mc.screen, DownloadResult.IO_FAILURE));
                break;

            default:
                break;
        }
    }

    /**
     * Called when a download finishes.
     */
    private static void onDownloadFinished() {

        Constants.LOG.debug("[CommonClass] onDownloadFinished called");

        // If the download wait screen is showing,
        // close it.
        final Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof final DownloadWaitScreen waitScreen) {
            waitScreen.onClose();
        }

        // Enable OPTIONAL_ENABLED resource packs by default
        // TODO: Enable them between user-added packs and forced packs
        final PackRepository repo = mc.getResourcePackRepository();
        repo.reload();
        final List<DependencyInfo> optionalEnabled = Config.INSTANCE.dependencies.stream()
                .filter(dependency -> dependency.mode() == DependencyInfo.Mode.OPTIONAL_ENABLED
                        && dependency.type() == DependencyInfo.ResourceClass.RESOURCE_PACK)
                .toList();
        final Set<String> selectedPacks = new LinkedHashSet<>(repo.getSelectedIds());
        selectedPacks.addAll(optionalEnabled.stream()
                .sorted(Comparator.comparingInt(v -> v.loadPriority()))
                .map(DependencyInfo::packId)
                .toList());
        repo.setSelected(selectedPacks);
    }
}