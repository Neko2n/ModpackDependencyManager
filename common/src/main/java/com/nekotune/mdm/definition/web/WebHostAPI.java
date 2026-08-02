package com.nekotune.mdm.definition.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import com.nekotune.mdm.definition.DependencyInfo;

public abstract class WebHostAPI {

    protected abstract APIResponse<byte[]> GET(final String slug,
            final DependencyInfo.ResourceClass resourceClass) throws
                    IOException, InterruptedException,
                    SecurityException;

    /**
     * Downloads a resource pack from this website.
     * 
     * @param slug The slug for the resource pack. This can be found in its URL.
     * @return The downloaded file's path, or {@link Optional#empty()} if the
     *         download failed.
     */
    public APIResponse<Path> fetch(final String slug, final DependencyInfo.ResourceClass resourceClass) throws
            IOException, InterruptedException, SecurityException {
        final Path destination = Path.of(resourceClass.folder, "modpack." + slug + ".zip");
        return downloadTo(destination, slug, resourceClass);
    }

    private APIResponse<Path> downloadTo(final Path destination, final String slug, final DependencyInfo.ResourceClass resourceClass)
            throws IOException, InterruptedException, SecurityException {

        // Fetch the best file
        final APIResponse<byte[]> response = GET(slug, resourceClass);

        // Validate response
        if (response.statusCode() != 200) {
            return new APIResponse<>(response.statusCode(), null);
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
        return new APIResponse<>(200, written);
    }

    protected static record APIResponse<T>(
            int statusCode,
            T body) {
    }
}
