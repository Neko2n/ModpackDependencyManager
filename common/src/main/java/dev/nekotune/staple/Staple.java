package dev.nekotune.staple;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dev.nekotune.staple.DownloadManager.Download;
import dev.nekotune.staple.definition.DependencyInfo;
import dev.nekotune.staple.platform.Services;
import dev.nekotune.staple.platform.services.IPlatformHelper;
import dev.nekotune.staple.platform.services.IPlatformHelper.Dist;
import dev.nekotune.staple.server.ServerStaple;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;

public final class Staple {

    public static void init() {
        final IPlatformHelper platform = Services.PLATFORM.get();
        if (platform.dist() == Dist.SERVER) {
            ServerStaple.init();
        }

        // Download dependencies
        (new DownloadTaskThread()).start();
    }

    /**
     * Utility method to automatically enable downloaded dependency packs marked as
     * OPTIONAL_ENABLED
     * 
     * @param repo The pack repository to enable the packs within
     * @param type The type of packs in the repository
     */
    public static void enableDownloadedOptionals(final PackRepository repo, final PackType type) {
        repo.reload();
        final List<DependencyInfo> optionalEnabled = DownloadManager.getDownloaded().stream()
                .filter(dependency -> dependency.mode() == DependencyInfo.Mode.OPTIONAL_ENABLED
                        && dependency.type() == type)
                .toList();
        final Set<String> selectedPacks = new LinkedHashSet<>(repo.getSelectedIds());
        selectedPacks.addAll(optionalEnabled.stream()
                .sorted(Comparator.comparingInt(v -> v.loadPriority()))
                .map(DependencyInfo::packId)
                .toList());
        repo.setSelected(selectedPacks);
    }

    /**
     * Task thread to handle downloading dependencies separately from the main
     * thread.
     * 
     * @see DownloadManager
     */
    public static final class DownloadTaskThread extends Thread {

        private final List<DependencyInfo> targets;

        private DownloadTaskThread() {

            // Build a list of download targets, excluding those already downloaded
            this.targets = new ArrayList<>(Config.INSTANCE.dependencies.stream()
                    .filter(target -> !target.isDownloaded())
                    .toList());
        }

        @Override
        public void run() {
            Constants.LOG.debug("[Staple] [Download Thread] Thread started");
            final Download threads = DownloadManager.dispatch(targets);
            final long startTime = System.currentTimeMillis();
            Constants.LOG.debug("[Staple] [Download Thread] Downloading dependencies...");
            try {
                threads.await();
            } catch (final InterruptedException e) {
                Constants.LOG.error("[Staple] [Download Thread] Dependencies download FAILURE; ", e);
                Thread.currentThread().interrupt();
                return;
            }
            final long downloadTime = System.currentTimeMillis() - startTime;

            // Report successful download
            String report = "[Staple] [Download Thread] Dependencies download SUCCESS; Took " + downloadTime
                    + " ms; Downloaded: [ ";
            for (final DependencyInfo target : DownloadManager.getDownloaded()) {
                report += target.slug() + ", ";
            }
            report = (report + "]").replace(", ]", " ]");
            Constants.LOG.debug(report);
        }
    }
}
