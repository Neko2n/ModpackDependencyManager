package dev.nekotune.mdm.definition.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import dev.nekotune.mdm.definition.DependencyInfo;

import net.minecraft.server.packs.PackType;

public abstract class WebHost {

    protected static final int HTTP_TIMEOUT_MILLIS = 30000;

    protected abstract APIResponse<byte[]> GET(final String slug,
            final PackType packType) throws
                    IOException, InterruptedException,
                    SecurityException;
    
    /**
     * Calls {@link APIResponse#GET(String, PackType)} with a timeout guard.
     */
    protected final APIResponse<byte[]> GET$timeout(final String slug,
            final PackType packType, final int millis) throws
                    IOException, InterruptedException,
                    SecurityException, TimeoutException {
        final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            final Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            final Future<APIResponse<byte[]>> future = executor.submit(() -> GET(slug, packType));
            try {
                return future.get(millis, TimeUnit.MILLISECONDS);
            } catch (final TimeoutException e) {
                future.cancel(true);
                throw e;
            } catch (final ExecutionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof final IOException io)
                    throw io;
                if (cause instanceof final SecurityException se)
                    throw se;
                if (cause instanceof final RuntimeException re)
                    throw re;
                throw new IOException("Unexpected error during GET for '" + slug + "'", cause);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Downloads a resource pack from this website.
     * 
     * @param slug The slug for the resource pack. This can be found in its URL.
     * @return The downloaded file's path, or {@link Optional#empty()} if the
     *         download failed.
     */
    public APIResponse<byte[]> download(final DependencyInfo target)
            throws IOException, InterruptedException, SecurityException, TimeoutException {

        // Send an http GET request for the best file
        APIResponse<byte[]> response = GET$timeout(target.slug(), target.type(), HTTP_TIMEOUT_MILLIS);
        for (final String mirror : target.mirrors()) {

            // Try mirror slugs on failure
            if (response.statusCode() != 200) {
                response = GET$timeout(mirror, target.type(), HTTP_TIMEOUT_MILLIS);
            } else {
                break;
            }
        }

        // Write file on success
        if (response.statusCode() == 200) {
            
            // Ensure parent directories exist
            final Path parent = target.packDir().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            // Write the file to disk
            Files.write(target.packDir(), response.body(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        }
        return response;
    }

    public static record APIResponse<T>(
            int statusCode,
            T body) {
    }
}
