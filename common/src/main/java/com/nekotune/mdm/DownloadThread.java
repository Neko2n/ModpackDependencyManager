package com.nekotune.mdm;

import java.util.ArrayList;
import java.util.List;

import com.nekotune.mdm.DownloadManager.DownloadThreads;
import com.nekotune.mdm.definition.DependencyInfo.DownloadTarget;

public final class DownloadThread extends Thread {

    private static final DownloadThread INSTANCE = new DownloadThread();

    public static volatile DownloadState state = DownloadState.NOT_STARTED;

    final List<DownloadTarget> targets;

    private DownloadThread() {

        // Build a list of download targets, excluding those already downloaded
        this.targets = new ArrayList<>(Config.INSTANCE.dependencies.stream()
                .map(settings -> new DownloadTarget(settings))
                .filter(target -> !Config.INSTANCE.downloaded.contains(target.slug()))
                .toList());
    }

    public static void dispatch() {
        INSTANCE.start();
    }

    @Override
    public void run() {
        state = DownloadState.STARTED;
        Constants.LOG.debug("[CommonClass] [Download Thread] Thread started");
        final DownloadThreads threads = DownloadManager.dispatch(targets);
        final long startTime = System.currentTimeMillis();
        Constants.LOG.debug("[CommonClass] [Download Thread] Downloading dependencies...");
        try {
            threads.await();
        } catch (final InterruptedException e) {
            Constants.LOG.error("[CommonClass] [Download Thread] Dependencies download FAILURE; ", e);
            state = DownloadState.FAILED;
            Thread.currentThread().interrupt();
            return;
        }
        final long downloadTime = System.currentTimeMillis() - startTime;

        // Report successful download
        state = DownloadState.FINISHED;
        String report = "[CommonClass] [Download Thread] Dependencies download SUCCESS; Took " + downloadTime + " ms; Downloaded: [ ";
        for (final DownloadTarget target : DownloadManager.getDownloaded()) {
            Config.INSTANCE.downloaded.add(target.slug());
            report += target.slug() + ", ";
        }
        report = (report + "]").replace(", ]", " ]");
        Constants.LOG.debug(report);
        
        // Save state to config file
        Config.INSTANCE.save();
    }

    public static enum DownloadState {
        NOT_STARTED,
        STARTED,
        FINISHED,
        FAILED;
    }
}