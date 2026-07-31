package com.nekotune.mdm.definition.web;

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

public abstract class Website {

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
    public Path fetchAssets(final String slug) throws
            FileNotFoundException, HttpRetryException, IOException,
            InterruptedException, SecurityException {
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
    public Path fetchData(final String slug) throws
            FileNotFoundException, HttpRetryException, IOException,
            InterruptedException, SecurityException {
        final Path destination = Path.of("datapacks", slug + ".zip");
        return downloadTo(destination, slug);
    }

    private Path downloadTo(final Path destination, final String slug) throws
            FileNotFoundException, HttpRetryException, IOException,
            InterruptedException, SecurityException {

        // Fetch the best file
        final String fileUrl = resolveFileURL(slug);
        final HttpResponse<byte[]> response = HTTP_CLIENT.send(
                buildRequest(fileUrl),
                HttpResponse.BodyHandlers.ofByteArray());

        // Validate response
        if (response.statusCode() != 200) {
            final String message = "File download failed: HTTP " + response.statusCode();
            throw new HttpRetryException(message, response.statusCode());
        }

        // Write the file to disk
        final Path written = Files.write(destination, response.body(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        return written;
    }

    protected HttpRequest buildRequest(final String url) {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url));
        this.requestHeaders().forEach((final String k, final String v) -> {
            requestBuilder.header(k, v);
        });
        return requestBuilder.GET().build();
    }

    protected HttpResponse<String> fetchString(final String url) throws
            FileNotFoundException, HttpRetryException, IOException,
            InterruptedException, SecurityException {
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
