package com.nekotune.mdm;

import java.util.List;

import com.nekotune.mdm.definition.DependencyInfo.DownloadTarget;
import com.nekotune.mdm.DownloadManager.DownloadThreads;
import com.nekotune.mdm.definition.DependencyInfo;

public class CommonClass {

    public static void init() {
        Constants.LOG.debug("[CommonClass] Hello from CommonClass#init");

        ConfigHandler.init();

        final Thread downloadTask = new Thread(() -> {
            Constants.LOG.debug("[CommonClass] [Download Thread] Thread started");
            final DownloadThreads threads = DownloadManager.dispatch(List.of(
                new DownloadTarget("fresh-animations",
                        List.of(),
                        DependencyInfo.Host.MODRINTH,
                        DependencyInfo.Mode.FORCED,
                        DependencyInfo.Type.RESOURCE_PACK),
                new DownloadTarget("invalid-slug-bl",
                        List.of("better-leaves"),
                        DependencyInfo.Host.MODRINTH,
                        DependencyInfo.Mode.FORCED,
                        DependencyInfo.Type.RESOURCE_PACK),
                new DownloadTarget("just-not-valid-at-all",
                        List.of(),
                        DependencyInfo.Host.MODRINTH,
                        DependencyInfo.Mode.FORCED,
                        DependencyInfo.Type.RESOURCE_PACK),
                new DownloadTarget("tras-fresh-player",
                        List.of(),
                        DependencyInfo.Host.MODRINTH,
                        DependencyInfo.Mode.FORCED,
                        DependencyInfo.Type.DATA_PACK)
            ));
            try {
                Constants.LOG.debug("[CommonClass] [Download Thread] Downloading dependencies...");
                threads.await();
                Constants.LOG.debug("[CommonClass] [Download Thread] Dependencies download SUCCESS");
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        downloadTask.start();
    }
}