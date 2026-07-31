package com.nekotune.mdm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpRetryException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import com.nekotune.mdm.definition.DependencyInfo;
import com.nekotune.mdm.definition.DependencyInfo.DownloadTarget;
import com.nekotune.mdm.definition.DependencyInfo.Host;
import com.nekotune.mdm.definition.web.Curseforge;
import com.nekotune.mdm.definition.web.Modrinth;
import com.nekotune.mdm.definition.web.Website;

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
     * @see Website
     */
    public static DownloadThreads dispatch(final List<DownloadTarget> targets) {
        final DownloadThreads threads = new DownloadThreads(targets);
        threads.forEach(DownloadThread::start);
        return threads;
    }

    public static final class DownloadThreads extends CountDownLatch implements Iterable<DownloadThread> {
        private final EnumMap<DependencyInfo.Host, DownloadThread> map =
                new EnumMap<>(DependencyInfo.Host.class);

        public DownloadThreads(final List<DownloadTarget> targets) {
            super(targets.size());
            assign(DependencyInfo.Host.MODRINTH, targets);
            assign(DependencyInfo.Host.CURSEFORGE, targets);
        }

        private DownloadThread assign(Host key, final List<DownloadTarget> targets) {
            return map.put(key, new DownloadThread(this,
                    targets.stream().filter(key::matches).toList()));
        }

        public Map<DependencyInfo.Host, DownloadThread> toMap() {
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
            final List<Website> websitesToTry = new LinkedList<>();
            switch (target.host()) {
                case ANY:
                    websitesToTry.add(Modrinth.INSTANCE);
                    websitesToTry.add(Curseforge.INSTANCE);
                    break;
                case MODRINTH:
                    websitesToTry.add(Modrinth.INSTANCE);
                    break;
                case CURSEFORGE:
                    websitesToTry.add(Curseforge.INSTANCE);
                    break;
            }
            final List<String> slugsToTry = List.copyOf(target.mirrors());
            slugsToTry.addFirst(target.slug());
            
            // Set up tracking variables
            int slugsTried = 0;
            int websitesTried = 0;
            int httpRetries = 0;

            // Attempt to download file from all slugs and websites
            while (true) {
                final String slug = slugsToTry.get(slugsTried);
                final Website website = websitesToTry.get(websitesTried);

                try {
                    switch (target.type()) {
                        case RESOURCE_PACK:
                            website.fetchAssets(slug);
                            break;
                        case DATA_PACK:
                            website.fetchData(slug);
                            break;
                        default:
                            throw new UnsupportedOperationException(
                                    "DownloadTarget type " + target.type() + " unsupported");
                    }
                    return DownloadResult.SUCCESS;
                } catch (final FileNotFoundException e) {
                    httpRetries = 0;

                    // If all slugs have been tried for this host,
                    // try other hosts.
                    if (slugsTried == slugsToTry.size()) {
                        slugsTried = 0;
                        websitesTried++;
                        continue;
                    }

                    // Try the next slug
                    slugsTried++;
                    continue;

                } catch (final HttpRetryException e) {
                    // Attempt to retry when prompted
                    httpRetries++;
                    if (httpRetries >= MAX_HTTP_RETRIES) {
                        return DownloadResult.OUT_OF_RETRIES;
                    }
                    continue;

                } catch (final IOException e) {
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
