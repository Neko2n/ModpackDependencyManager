package com.nekotune.mdm.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpRetryException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public abstract class WebHostAPI {

    protected abstract HttpResponse<byte[]> GET(final String slug,
            final ResourceClass resourceClass) throws
                    FileNotFoundException, HttpRetryException, IOException,
                    InterruptedException, SecurityException;

    /**
     * Downloads a resource pack from this website.
     * 
     * @param slug The slug for the resource pack. This can be found in its URL.
     * @return The downloaded file's path, or {@link Optional#empty()} if the
     *         download failed.
     */
    public Path fetchAssets(final String slug) throws
            FileNotFoundException, HttpRetryException, IOException,
            InterruptedException, SecurityException {
        final Path destination = Path.of("resourcepacks", slug + ".zip");
        return downloadTo(destination, slug, ResourceClass.RESOURCE_PACK);
    }

    /**
     * Downloads a data pack from this website.
     * 
     * @param slug The slug for the data pack. This can be found in its URL.
     * @return The downloaded file's path, or {@link Optional#empty()} if the
     *         download failed.
     */
    public Path fetchData(final String slug) throws
            FileNotFoundException, HttpRetryException, IOException,
            InterruptedException, SecurityException {
        final Path destination = Path.of("datapacks", slug + ".zip");
        return downloadTo(destination, slug, ResourceClass.DATA_PACK);
    }

    private Path downloadTo(final Path destination, final String slug, final ResourceClass resourceClass) throws
            FileNotFoundException, HttpRetryException, IOException,
            InterruptedException, SecurityException {

        // Fetch the best file
        final HttpResponse<byte[]> response = GET(slug, resourceClass);

        // Validate response
        if (response.statusCode() != 200) {
            final String message = "File download failed: HTTP " + response.statusCode();
            throw new HttpRetryException(message, response.statusCode());
        }

        // Ensure parent directories exist
        final Path parent = destination.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        // Write the file to disk
        final Path written = Files.write(destination, response.body(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        return written;
    }

    protected static enum ResourceClass {
        RESOURCE_PACK,
        DATA_PACK;
    }
}
