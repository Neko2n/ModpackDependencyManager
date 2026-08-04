package com.nekotune.mdm;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import com.nekotune.mdm.definition.DependencyInfo;
import com.nekotune.mdm.definition.Event;
import com.nekotune.mdm.definition.web.WebHostAPI;
import com.nekotune.mdm.definition.web.WebHostAPI.APIResponse;

public final class DownloadManager {

    public static final Event onDownloadFinished = new Event();

    /**
     * A list of errors that occured during the download process.
     * Errors are mapped by the slug belonging to the dependency that errored.
     */
    public static final Map<String, DownloadResult> DOWNLOAD_ERRORS = new HashMap<>();

    /**
     * A list of download targets which have been successfully downloaded
     * in the current session.
     */
    private static final Map<String, DependencyInfo> DOWNLOADED = new HashMap<>();

    /**
     * @see DownloadManager#DOWNLOADED
     */
    public static Collection<DependencyInfo> getDownloaded() {
        return DOWNLOADED.values();
    }

    /**
     * @return True if the dependency with the given target was downloaded
     *         in the running session, false otherwise.
     */
    public static boolean wasDownloaded(final DependencyInfo target) {
        return DOWNLOADED.containsKey(target.slug());
    }

    /**
     * Dispatches worker threads to fetch the provided dependency targets.
     * Populates the {@link DownloadManager#DOWNLOADED} map.
     * 
     * @param targets A list of dependency targets to download
     *                from the web.
     * @return The scheduled worker threads bundled as a DownloadThreads object.
     * @see WebHostAPI
     */
    public static Download dispatch(final List<DependencyInfo> targets) {
        final Download threads = new Download(targets);
        threads.forEach(WorkerThread::start);
        return threads;
    }

    public static final class Download extends CountDownLatch implements Iterable<WorkerThread> {
        private final EnumMap<DependencyInfo.Host, WorkerThread> map =
                new EnumMap<>(DependencyInfo.Host.class);

        public Download(final List<DependencyInfo> targets) {
            super(DependencyInfo.Host.values().length);
            for (DependencyInfo.Host website : DependencyInfo.Host.values()) {
                assign(website, targets);
            }
        }

        private WorkerThread assign(final DependencyInfo.Host host, final List<DependencyInfo> targets) {
            return map.put(host, new WorkerThread(this,
                    targets.stream().filter(host::matches).toList()));
        }

        public Map<DependencyInfo.Host, WorkerThread> toMap() {
            return map;
        }

        @Override
        public Iterator<WorkerThread> iterator() {
            return map.values().iterator();
        }
    }

    private static final class WorkerThread extends Thread {

        private static final Set<DependencyInfo> handled = new HashSet<>();
        private static final Semaphore sem = new Semaphore(1);

        private final CountDownLatch latch;
        private final Set<DependencyInfo> targets;

        private WorkerThread(final CountDownLatch latch,
                final Collection<DependencyInfo> targets) {
            this.latch = latch;
            this.targets = Set.copyOf(targets);
        }

        private void exit() {
            latch.countDown();
        }

        @Override
        public void run() {
            for (final DependencyInfo target : targets) {

                // Ensure target is not already handled by another thread
                sem.acquireUninterruptibly();
                if (handled.contains(target)) {
                    sem.release();
                    continue;
                }
                handled.add(target);
                sem.release();

                // Download dependency target
                final DownloadResult result;
                try {
                    result = tryDownload(target);
                } catch (final InterruptedException e) {
                    exit();
                    this.interrupt();
                    return;
                }
                switch (result) {
                    case SUCCESS:
                        Constants.LOG.debug("[DownloadManager] [Working Thread " + this.threadId() + "] Download SUCCESS for target " + target.toString());
                        DOWNLOADED.put(target.slug(), target);
                        break;
                    case NOT_FOUND:
                        Constants.LOG.warn("[DownloadManager] [Working Thread " + this.threadId() + "] Download FAILURE; No files found for target " + target.toString() + "; Report this to the modpack author");
                        DOWNLOAD_ERRORS.put(target.slug(), result);
                        break;
                    case SECURITY_BLOCKED:
                        Constants.LOG.warn("[DownloadManager] [Working Thread " + this.threadId() + "] Download FAILURE; Downloads blocked by security permissions; Check your firewall settings");
                        DOWNLOAD_ERRORS.put(target.slug(), result);
                        break;
                    case EXCEPTION:
                        Constants.LOG.error("[DownloadManager] [Working Thread " + this.threadId() + "] Download FAILURE; An exception occured");
                        DOWNLOAD_ERRORS.put(target.slug(), result);
                        break;
                }
            }
            exit();
        }

        private static DownloadResult tryDownload(final DependencyInfo target)
                throws InterruptedException {

            // Attempt to download file from all websites
            for (final DependencyInfo.Host host : target.hosts()) {
                final APIResponse<byte[]> response;

                try {
                    response = host.get().download(target);
                } catch (final SecurityException e) {
                    Constants.LOG.error("[DownloadManager#tryDownload] " + e.toString());
                    return DownloadResult.SECURITY_BLOCKED;
                } catch (final IOException e) {
                    Constants.LOG.error("[DownloadManager#tryDownload] " + e.toString());
                    return DownloadResult.EXCEPTION;
                }

                // Report a successful download
                if (response.statusCode() == 200) {
                    return DownloadResult.SUCCESS;
                }
                
                // Report that there was no file at the given slugs
                if (response.statusCode() == 404) {
                    Constants.LOG.warn("[DownloadManager#tryDownload] Download failed; HTTP 404; No files found for dependency " + target.toString());
                    return DownloadResult.NOT_FOUND;
                }
            }

            return DownloadResult.EXCEPTION;
        }
    }

    public static enum DownloadResult {
        SUCCESS,
        NOT_FOUND,
        EXCEPTION,
        SECURITY_BLOCKED;
    }
}
