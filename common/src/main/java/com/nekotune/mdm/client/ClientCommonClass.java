package com.nekotune.mdm.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

import com.nekotune.mdm.Config;
import com.nekotune.mdm.Constants;
import com.nekotune.mdm.DownloadManager;
import com.nekotune.mdm.DownloadManager.DownloadResult;
import com.nekotune.mdm.DownloadTaskThread;
import com.nekotune.mdm.client.gui.DownloadErrorScreen;
import com.nekotune.mdm.client.gui.DownloadWaitScreen;
import com.nekotune.mdm.client.gui.ReloadPromptScreen;
import com.nekotune.mdm.definition.DependencyInfo;
import com.nekotune.mdm.definition.DependencyInfo.ResourceClass;
import com.nekotune.mdm.mixin.minecraft.TitleScreenMixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.packs.repository.PackRepository;

public class ClientCommonClass {

    private static final Semaphore lock = new Semaphore(1);

    public static void init() {

        DownloadManager.onDownloadFinished.connect(() -> {
            Constants.LOG.debug("[ClientCommonClass] onDownloadFinished called");
            lock.acquireUninterruptibly();

            // If the download wait screen is showing,
            // change it to the reload prompt.
            final Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof final DownloadWaitScreen waitScreen) {
                waitScreen.onClose();
                mc.setScreen(new ReloadPromptScreen(mc.screen));
            }

            lock.release();

            // Enable OPTIONAL_ENABLED packs by default
            final BiConsumer<ResourceClass, PackRepository> enableOptionals = (resourceType, repo) -> {
                repo.reload();
                final List<DependencyInfo> optionalEnabled = Config.INSTANCE.dependencies.stream()
                        .filter(dependency -> dependency.mode() == DependencyInfo.Mode.OPTIONAL_ENABLED
                                && dependency.type() == resourceType)
                        .toList();
                final Set<String> selectedPacks = new LinkedHashSet<>(repo.getSelectedIds());
                selectedPacks.addAll(optionalEnabled.stream()
                        .sorted(Comparator.comparingInt(v -> v.loadPriority()))
                        .map(DependencyInfo::packId)
                        .toList());
                repo.setSelected(selectedPacks);
            };
            enableOptionals.accept(ResourceClass.RESOURCE_PACK,
                    mc.getResourcePackRepository());
            final IntegratedServer server = mc.getSingleplayerServer();
            if (server != null) {
                enableOptionals.accept(ResourceClass.DATA_PACK,
                        server.getPackRepository());
            }
        });
    }
    
    /**
     * Called the first time the game reaches the title screen after loading.
     * 
     * @see TitleScreenMixin#mdm$onTitleScreen
     */
    public static void clientLoaded(final Minecraft mc) {
        Constants.LOG.debug("[CommonClass] clientLoaded called");
        lock.acquireUninterruptibly();

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

        lock.release();
    }
}
