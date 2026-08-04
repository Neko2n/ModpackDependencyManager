package com.nekotune.mdm.definition.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import com.nekotune.mdm.definition.DependencyInfo;

import net.minecraft.server.packs.PackType;

public abstract class WebHostAPI {

    protected abstract APIResponse<byte[]> GET(final String slug,
            final PackType packType) throws
                    IOException, InterruptedException,
                    SecurityException;
         
    /**
     * Downloads a resource pack from this website.
     * 
     * @param slug The slug for the resource pack. This can be found in its URL.
     * @return The downloaded file's path, or {@link Optional#empty()} if the
     *         download failed.
     */
    public APIResponse<byte[]> download(final DependencyInfo target)
            throws IOException, InterruptedException, SecurityException {

        // Send an http GET request for the best file
        APIResponse<byte[]> response = GET(target.slug(), target.type());
        for (final String mirror : target.mirrors()) {

            // Try mirror slugs on failure
            if (response.statusCode() != 200) {
                response = GET(mirror, target.type());
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
