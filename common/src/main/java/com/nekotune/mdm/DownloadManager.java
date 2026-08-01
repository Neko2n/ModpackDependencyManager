package com.nekotune.mdm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpRetryException;
import java.util.ArrayList;
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

import com.nekotune.mdm.definition.DependencyInfo.DownloadTarget;
import com.nekotune.mdm.definition.web.WebHostAPI;

public final class DownloadManager {

    /**
     * A list of download targets which have been successfully downloaded.
     * Targets listed here will be excluded from the download process on startup.
     */
    private static final Map<String, DownloadTarget> DOWNLOADED = new HashMap<>();

    /**
     * @return A copy of {@link DownloadManager#DOWNLOADED}
     */
    public static Collection<DownloadTarget> getDownloaded() {
        return DOWNLOADED.values();
    }

    /**
     * @return True if the dependency with the given target is downloaded,
     *         false otherwise.
     */
    public static boolean isDownloaded(final DownloadTarget target) {
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
    public static DownloadThreads dispatch(final List<DownloadTarget> targets) {
        final DownloadThreads threads = new DownloadThreads(targets);
        threads.forEach(DownloadThread::start);
        return threads;
    }

    public static final class DownloadThreads extends CountDownLatch implements Iterable<DownloadThread> {
        private final EnumMap<DownloadTarget.Host, DownloadThread> map =
                new EnumMap<>(DownloadTarget.Host.class);

        public DownloadThreads(final List<DownloadTarget> targets) {
            super(DownloadTarget.Host.values().length);
            for (DownloadTarget.Host website : DownloadTarget.Host.values()) {
                assign(website, targets);
            }
        }

        private DownloadThread assign(final DownloadTarget.Host host, final List<DownloadTarget> targets) {
            return map.put(host, new DownloadThread(this,
                    targets.stream().filter(host::matches).toList()));
        }

        public Map<DownloadTarget.Host, DownloadThread> toMap() {
            return map;
        }

        @Override
        public Iterator<DownloadThread> iterator() {
            return map.values().iterator();
        }
    }

    public static final class DownloadThread extends Thread {

        public static final int MAX_HTTP_RETRIES = 20;

        private static final Set<DownloadTarget> handled = new HashSet<>();
        private static final Semaphore sem = new Semaphore(1);

        private final CountDownLatch latch;
        private final Set<DownloadTarget> targets;

        private DownloadThread(final CountDownLatch latch,
                final Collection<DownloadTarget> targets) {
            this.latch = latch;
            this.targets = Set.copyOf(targets);
        }

        @Override
        public void run() {
            for (final DownloadTarget target : targets) {

                // Ensure target is not already handled by another thread
                try {
                    sem.acquire();
                } catch (final InterruptedException e) {
                    Constants.LOG.error(e.toString());
                    return;
                }
                if (handled.contains(target)) {
                    sem.release();
                    continue;
                }
                handled.add(target);
                sem.release();

                // Download dependency target
                final DownloadResult result = tryDownload(target);
                switch (result) {
                    case SUCCESS:
                        Constants.LOG.debug("[DownloadManager] [Working Thread " + this.threadId() + "] Download SUCCESS for target " + target.toString());
                        DOWNLOADED.put(target.slug(), target);
                        break;
                    case NOT_FOUND:
                        Constants.LOG.warn("[DownloadManager] [Working Thread " + this.threadId() + "] Download FAILURE; No files found for target " + target.toString() + "; Report this to the modpack author");
                        break;
                    case SECURITY_BLOCKED:
                        Constants.LOG.warn("[DownloadManager] [Working Thread " + this.threadId() + "] Download FAILURE; Downloads blocked by security permissions; Check your firewall settings");
                        break;
                    case OUT_OF_RETRIES:
                        Constants.LOG.error("[DownloadManager] [Working Thread " + this.threadId() + "] Download FAILURE; Ran out of HTTP request attempts for target " + target.toString() + "; Report this to the issue tracker");
                        break;
                    case IO_FAILURE:
                        Constants.LOG.error("[DownloadManager] [Working Thread " + this.threadId() + "] Download FAILURE; IO failure while trying to write file; These things happen");
                        break;
                }
            }
            latch.countDown();
        }

        private static DownloadResult tryDownload(final DownloadTarget target) {
            
            // Determine download host(s) and target slug(s)
            final List<DownloadTarget.Host> websitesToTry = target.hosts();
            final List<String> slugsToTry = new ArrayList<>(target.mirrors());
            slugsToTry.addFirst(target.slug());
            
            // Set up tracking variables
            int slugsTried = 0;
            int websitesTried = 0;
            int httpRetries = 0;

            // Attempt to download file from all slugs and websites
            while (true) {
                final String slug = slugsToTry.get(slugsTried);
                final WebHostAPI website = websitesToTry.get(websitesTried).get();

                try {
                    website.fetch(slug, target.type());
                    return DownloadResult.SUCCESS;
                } catch (final FileNotFoundException e) {
                    httpRetries = 0;
                    slugsTried++;

                    // If all slugs have been tried for this host,
                    // try other hosts.
                    if (slugsTried == slugsToTry.size()) {
                        slugsTried = 0;
                        websitesTried++;

                        // If all hosts have been tried, return NOT_FOUND
                        if (websitesTried == websitesToTry.size()) {
                            return DownloadResult.NOT_FOUND;
                        }
                    }
                    continue;

                } catch (final HttpRetryException e) {
                    // Attempt to retry when prompted
                    httpRetries++;
                    if (httpRetries >= MAX_HTTP_RETRIES) {
                        return DownloadResult.OUT_OF_RETRIES;
                    }
                    continue;

                } catch (final IOException e) {
                    Constants.LOG.error("[DownloadManager#tryDownload] " + e.toString());
                    return DownloadResult.IO_FAILURE;

                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private static enum DownloadResult {
            SUCCESS,
            NOT_FOUND,
            OUT_OF_RETRIES,
            IO_FAILURE,
            SECURITY_BLOCKED;
        }
    }
}
