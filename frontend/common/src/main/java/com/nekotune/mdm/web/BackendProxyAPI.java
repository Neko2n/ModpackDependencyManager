package com.nekotune.mdm.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpRetryException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.nekotune.mdm.Constants;

public abstract class BackendProxyAPI extends WebHostAPI {

    private static final String SERVER = "http://localhost:8080/";

    public final String path;

    protected BackendProxyAPI(final String path) {
        this.path = path;
    }
    
    @Override
    protected final HttpResponse<byte[]> GET(final String slug,
            final ResourceClass resourceClass) throws
                    FileNotFoundException, HttpRetryException, IOException,
                    InterruptedException, SecurityException {
        final HttpResponse<byte[]> response = Constants.HTTP_CLIENT.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(SERVER + this.path + "/" + slug))
                        .header("GameVersions",
                                String.join(",", Constants.MC_VERSIONS))
                        .header("ResourceClass", resourceClass.name())
                        .build(),
                HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() == 404) {
            throw new FileNotFoundException("File not found: " + this.path + "/" + slug);
        } else if (response.statusCode() != 200) {
            final String message = "File download failed: HTTP " + response.statusCode();
            throw new HttpRetryException(message, response.statusCode());
        }
        return response;
    }
}
