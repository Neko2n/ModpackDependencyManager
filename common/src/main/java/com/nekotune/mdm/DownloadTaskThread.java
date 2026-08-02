package com.nekotune.mdm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.nekotune.mdm.DownloadManager.Download;
import com.nekotune.mdm.definition.DependencyInfo;

public final class DownloadTaskThread implements Runnable {

    private static final DownloadTaskThread INSTANCE = new DownloadTaskThread();

    public static volatile DownloadState state = DownloadState.NOT_STARTED;

    private final List<DependencyInfo> targets;

    private DownloadTaskThread() {

        // Build a list of download targets, excluding those already downloaded
        this.targets = new ArrayList<>(Config.INSTANCE.dependencies.stream()
                .filter(target -> !target.isDownloaded())
                .toList());
    }

    public static CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(INSTANCE);
    }

    @Override
    public void run() {
        state = DownloadState.STARTED;
        Constants.LOG.debug("[CommonClass] [Download Thread] Thread started");
        final Download threads = DownloadManager.dispatch(targets);
        final long startTime = System.currentTimeMillis();
        Constants.LOG.debug("[CommonClass] [Download Thread] Downloading dependencies...");
        try {
            threads.await();
        } catch (final InterruptedException e) {
            Constants.LOG.error("[CommonClass] [Download Thread] Dependencies download FAILURE; ", e);
            state = DownloadState.INTERRUPTED;
            Thread.currentThread().interrupt();
            return;
        }
        final long downloadTime = System.currentTimeMillis() - startTime;

        // Report successful download
        state = DownloadState.FINISHED;
        String report = "[CommonClass] [Download Thread] Dependencies download SUCCESS; Took " + downloadTime + " ms; Downloaded: [ ";
        for (final DependencyInfo target : DownloadManager.getDownloaded()) {
            report += target.slug() + ", ";
        }
        report = (report + "]").replace(", ]", " ]");
        Constants.LOG.debug(report);
        
        // Save state to config file
        Config.INSTANCE.save();

        // Alert event listeners
        DownloadManager.onDownloadFinished.fire();
    }

    public static enum DownloadState {
        NOT_STARTED,
        STARTED,
        FINISHED,
        INTERRUPTED;
    }
}