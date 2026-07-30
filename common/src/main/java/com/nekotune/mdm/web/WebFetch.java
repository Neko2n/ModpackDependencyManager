package com.nekotune.mdm.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpRetryException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;

import com.nekotune.mdm.Constants;

public abstract class WebFetch {

    static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    protected abstract Map<String, String> requestHeaders();

    protected abstract String resolveFileURL(final String slug)
            throws IOException, InterruptedException, SecurityException;

    /**
     * Downloads a resource pack from this website.
     * 
     * @param slug The slug for the resource pack. This can be found in its URL.
     * @return The downloaded file's path, or {@link Optional#empty()} if the
     *         download failed.
     */
    public Optional<Path> downloadAssets(final String slug) {
        final Path destination = Path.of("resourcepacks", slug + ".zip");
        return downloadTo(destination, slug);
    }

    /**
     * Downloads a data pack from this website.
     * 
     * @param slug The slug for the data pack. This can be found in its URL.
     * @return The downloaded file's path, or {@link Optional#empty()} if the
     *         download failed.
     */
    public Optional<Path> downloadData(final String slug) {
        final Path destination = Path.of("datapacks", slug + ".zip");
        return downloadTo(destination, slug);
    }

    private Optional<Path> downloadTo(final Path destination, final String slug) {

        // Fetch the best file
        final HttpResponse<byte[]> response;
        try {
            final String fileUrl = resolveFileURL(slug);
            response = HTTP_CLIENT.send(buildRequest(fileUrl),
                    HttpResponse.BodyHandlers.ofByteArray());
        } catch (final FileNotFoundException e) {
            Constants.LOG.warn("[WebFetch] " + e.toString());
            // TODO: hook for warning about invalid slugs
            return Optional.empty();
        } catch (final IOException | InterruptedException | SecurityException e) {
            Constants.LOG.error("[WebFetch] Exception occured fetching file: " + e.toString());
            return Optional.empty();
        }

        // Validate response
        if (response.statusCode() != 200) {
            Constants.LOG.error("[WebFetch] File download failed: HTTP " + response.statusCode());
            return Optional.empty();
        }

        // Write the file to disk
        final Path written;
        try {
            written = Files.write(destination, response.body(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (final IOException e) {
            Constants.LOG.error("[WebFetch] " + e.toString());
            return Optional.empty();
        }
        return Optional.of(written);
    }

    protected HttpRequest buildRequest(final String url) {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url));
        this.requestHeaders().forEach((final String k, final String v) -> {
            requestBuilder.header(k, v);
        });
        return requestBuilder.GET().build();
    }

    protected HttpResponse<String> fetchString(final String url)
            throws IOException, InterruptedException, SecurityException {
        final HttpResponse<String> response = HTTP_CLIENT.send(
                buildRequest(url),
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 204 || response.statusCode() == 404) {
            throw new FileNotFoundException(response.statusCode() + "; File not found at url " + url);
        }
        if (response.statusCode() != 200) {
            throw new HttpRetryException("API error: " + response.body(), response.statusCode());
        }
        return response;
    }
}
