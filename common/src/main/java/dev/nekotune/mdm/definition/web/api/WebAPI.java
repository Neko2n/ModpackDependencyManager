package dev.nekotune.mdm.definition.web.api;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

public interface WebAPI {

    HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static <T> APIResponse<T> request(final String url, final String[] headers, final BodyHandler<T> bodyHandler)
            throws IOException, InterruptedException, SecurityException {
        final HttpResponse<T> response = WebAPI.HTTP_CLIENT.send(HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers(headers)
                .build(), bodyHandler);

        // If rate limited, try again.
        if (response.statusCode() == 429) {
            final Optional<String> retryAfter = response.headers().firstValue("Retry-After");
            int seconds;
            try {
                seconds = Integer.valueOf(retryAfter.orElse("2"));
            } catch (final NumberFormatException e) {
                seconds = 2;
            }
            Thread.sleep(Duration.ofSeconds(seconds));
            return request(url, headers, bodyHandler);
        }

        return new APIResponse<T>(response.statusCode(), response.body());
    }

    public static APIResponse<Path> download(final String url, final String[] headers, final Path downloadTo)
            throws IOException, InterruptedException, SecurityException {
        if (downloadTo.getParent() != null) {
            Files.createDirectories(downloadTo.getParent());
        }
        final APIResponse<InputStream> response = request(url, headers, BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            return new APIResponse<>(response.statusCode(), null);
        }
        try (InputStream inputStream = response.body();
                FileOutputStream fileOutputStream = new FileOutputStream(downloadTo.toFile())) {
            final byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }
        return new APIResponse<>(200, downloadTo);
    }

    public static record APIResponse<T>(
            int statusCode,
            T body) {
    }
}
