package dev.nekotune.mdm.definition.web.api;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public interface WebAPI {

    HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static APIResponse<Path> download(final HttpRequest request, final Path downloadTo)
            throws IOException, InterruptedException, SecurityException {
        if (downloadTo.getParent() != null) {
            Files.createDirectories(downloadTo.getParent());
        }
        final HttpResponse<InputStream> response = WebAPI.HTTP_CLIENT.send(request,
                HttpResponse.BodyHandlers.ofInputStream());
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
