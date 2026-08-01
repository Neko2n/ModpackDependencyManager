package com.nekotune.mdm;

import java.util.ArrayList;
import java.util.List;

import com.nekotune.mdm.definition.DependencyInfo.DownloadTarget;
import com.nekotune.mdm.DownloadManager.DownloadThreads;

public class CommonClass {

    public static void init() {
        Constants.LOG.debug("[CommonClass] Hello from CommonClass#init");

        final Config config = Config.INSTANCE;
        config.load();

        // Build a list of download targets, excluding those already downloaded
        final List<DownloadTarget> targets = new ArrayList<>(config.dependencies.stream()
                .map(settings -> new DownloadTarget(settings))
                .filter(target -> !config.downloaded.contains(target.slug()))
                .toList());

        // Download dependencies
        final Thread downloadTask = new Thread(() -> {
            Constants.LOG.debug("[CommonClass] [Download Thread] Thread started");
            final DownloadThreads threads = DownloadManager.dispatch(targets);
            try {
                final long startTime = System.currentTimeMillis();
                Constants.LOG.debug("[CommonClass] [Download Thread] Downloading dependencies...");
                threads.await();
                final long downloadTime = System.currentTimeMillis() - startTime;
                String report = "[CommonClass] [Download Thread] Dependencies download SUCCESS; Took " + downloadTime + " ms; Downloaded: [ ";
                for (final DownloadTarget target : DownloadManager.getDownloaded()) {
                    config.downloaded.add(target.slug());
                    report += target.slug() + ", ";
                }
                report = (report + "]").replace(", ]", " ]");
                Constants.LOG.debug(report);
                config.save();
            } catch (final InterruptedException e) {
                Constants.LOG.error("[CommonClass] [Download Thread] Dependencies download FAILURE; ", e);
                Thread.currentThread().interrupt();
            }
        });
        downloadTask.start();
    }
}