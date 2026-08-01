package com.nekotune.mdm.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpRetryException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nekotune.mdm.Constants;

public final class Modrinth extends WebHostAPI {

    public static final Modrinth INSTANCE = new Modrinth();

    private Modrinth() {
    }

    private final String VERSIONS_URL = "https://api.modrinth.com/v2/project/%s/version";

    @Override
    protected HttpResponse<byte[]> GET(final String slug,
            final ResourceClass resourceClass) throws
                    IOException, InterruptedException,
                    SecurityException {
        final String fileUrl = resolveFileURL(slug);
        return Constants.HTTP_CLIENT.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(fileUrl))
                        .headers(requestHeaders().entrySet().stream()
                                .flatMap(e -> Arrays.asList(e.getKey(), e.getValue()).stream())
                                .toArray(String[]::new))
                        .build(),
                HttpResponse.BodyHandlers.ofByteArray());
    }
    
    private Map<String, String> requestHeaders() {
        return Map.of(
                "User-Agent", "nekotune/" + Constants.MOD_ID + " (nekotune2n@gmail.com)");
    }

    private String resolveFileURL(final String slug)
            throws IOException, InterruptedException, SecurityException {

        // Fetch a list of version URLs
        final String url = VERSIONS_URL.formatted(slug);
        final HttpResponse<String> response = fetchString(url);
        final JsonArray versions = JsonParser.parseString(response.body()).getAsJsonArray();
        if (versions.isEmpty()) {
            throw new HttpRetryException("No versions found for project " + url, response.statusCode());
        }

        // Return newest file for the running game version
        JsonObject version;
        for (final JsonElement versionElement : versions) {

            // Find matching version
            version = versionElement.getAsJsonObject();
            final JsonArray gameVersions = version.getAsJsonArray("game_versions");
            boolean matches = false;
            for (final JsonElement gameVersion : gameVersions) {
                for (final String mcVersion : Constants.MC_VERSIONS) {
                    if (gameVersion.getAsString().equals(mcVersion)) {
                        matches = true;
                        break;
                    }
                }
                if (matches)
                    break;
            }

            // If there's a matching version, return it
            if (matches) {
                final JsonArray files = version.getAsJsonArray("files");
                for (final JsonElement element : files) {
                    final JsonObject fileJson = element.getAsJsonObject();
                    if (fileJson.get("primary").getAsBoolean()) {
                        return fileJson.get("url").getAsString();
                    }
                }
                return files.get(0).getAsJsonObject().get("url").getAsString();
            }
        }
        Constants.LOG.debug("[WebFetch] Version for minecraft "
                + Arrays.deepToString(Constants.MC_VERSIONS)
                + " not found at url + "
                + url
                + "; using primary version.");

        // Return primary version
        final JsonArray files = versions.get(0).getAsJsonObject()
                .getAsJsonArray("files");
        for (final JsonElement element : files) {
            final JsonObject fileJson = element.getAsJsonObject();
            if (fileJson.get("primary").getAsBoolean()) {
                return fileJson.get("url").getAsString();
            }
        }
        Constants.LOG.debug("[WebFetch] No primary version found at url + "
                + url
                + "; using latest version.");

        // Fall back to newest version
        return files.get(0).getAsJsonObject().get("url").getAsString();
    }

    private HttpResponse<String> fetchString(final String url) throws
            FileNotFoundException, HttpRetryException, IOException,
            InterruptedException, SecurityException {
        final HttpResponse<String> response = Constants.HTTP_CLIENT.send(
                HttpRequest.newBuilder().uri(URI.create(url)).build(),
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