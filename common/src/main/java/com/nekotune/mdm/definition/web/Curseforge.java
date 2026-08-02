package com.nekotune.mdm.definition.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nekotune.mdm.Constants;
import com.nekotune.mdm.definition.DependencyInfo;

public class Curseforge extends WebHostAPI {

    public static final Curseforge INSTANCE = new Curseforge();

    private static final int MINECRAFT_GAME_ID = 432;
    private static final String API_URL = "https://api.curseforge.com/v1";
    private static final String CATEGORIES_URL = API_URL + "/categories?gameId=" + MINECRAFT_GAME_ID
            + "&classesOnly=true";
    private static final String SEARCH_URL = API_URL + "/mods/search?gameId=" + MINECRAFT_GAME_ID + "&slug=%s";
    private static final String FILES_URL = API_URL + "/mods/%d/files";
    private static final String DOWNLOAD_URL = FILES_URL + "/%d/download-url";

    private static final String API_KEY = "$2a$10$H0H1nUTWnyquwe63X/2BsuRahDATysk9ub4kI2KgVeuUppffsGLji";

    private static final Map<DependencyInfo.ResourceClass, String> CONTENT_TYPE_MAP =
            new EnumMap<>(Map.of(
                    DependencyInfo.ResourceClass.RESOURCE_PACK, "Resource Packs",
                    DependencyInfo.ResourceClass.DATA_PACK, "Data Packs"
            ));
    private static final Map<DependencyInfo.ResourceClass, Integer> CLASS_ID_CACHE =
            new EnumMap<>(DependencyInfo.ResourceClass.class);

    @Override
    public APIResponse<byte[]> GET(final String slug, final DependencyInfo.ResourceClass resourceClass) throws
                    IOException, InterruptedException {
        final APIResponse<String> fileUrl = resolveFileURL(slug, Constants.MC_VERSIONS, resourceClass);
        if (fileUrl.body().isEmpty()) {
            return new APIResponse<>(404, new byte[0]);
        }
        final HttpResponse<byte[]> response = Constants.HTTP_CLIENT.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(fileUrl.body()))
                        .headers(requestHeaders().entrySet().stream()
                                .flatMap(e -> Arrays.asList(e.getKey(), e.getValue()).stream())
                                .toArray(String[]::new))
                        .build(),
                HttpResponse.BodyHandlers.ofByteArray());
        return new APIResponse<>(response.statusCode(), response.body());
    }

    private Map<String, String> requestHeaders() {
        return Map.of(
                "x-api-key", API_KEY,
                "Accept", "application/json");
    }

    private APIResponse<String> resolveFileURL(final String slug,
            final String[] targetGameVersions, final DependencyInfo.ResourceClass resourceClass) throws
                    IOException, InterruptedException {

        // Fetch a list of files for the given mod slug
        final APIResponse<Integer> assetIdResponse = resolveAssetId(slug, resourceClass);
        if (assetIdResponse.statusCode() != 200) {
            return new APIResponse<>(404, "");
        }
        final int assetId = assetIdResponse.body();
        final String url = FILES_URL.formatted(assetId);
        final APIResponse<String> response = fetchString(url);
        if (response.statusCode() != 200) {
            return new APIResponse<>(response.statusCode(), "");
        }
        final JsonArray files = JsonParser.parseString(response.body())
                .getAsJsonObject()
                .getAsJsonArray("data");
        if (files.isEmpty()) {
            return new APIResponse<>(404, "");
        }

        Optional<JsonObject> chosen = Optional.empty();

        // Choose newest file for the running game version
        for (final JsonElement fileElement : files) {
            final JsonObject file = fileElement.getAsJsonObject();
            if (!file.get("isAvailable").getAsBoolean()) {
                continue; // Author disabled downloads (archived)
            }
            final JsonArray gameVersions = file.getAsJsonArray("gameVersions");
            for (final JsonElement gameVersion : gameVersions) {
                for (final String targetGameVersion : targetGameVersions) {
                    if (gameVersion.getAsString().equals(targetGameVersion)) {
                        chosen = Optional.of(file);
                        break;
                    }
                }
                if (chosen.isPresent())
                    break;
            }
            if (chosen.isPresent())
                break;
        }

        // Choose newest available file
        if (chosen.isEmpty()) {
            for (final JsonElement fileElement : files) {
                final JsonObject file = fileElement.getAsJsonObject();
                if (file.get("isAvailable").getAsBoolean()) {
                    chosen = Optional.of(file);
                    break;
                }
            }
        }

        if (chosen.isEmpty()) {
            return new APIResponse<>(404, "");
        }
        final JsonObject file = chosen.orElseThrow();

        // Use download URL if author has enabled third-party downloads
        if (file.has("downloadUrl")) {
            return new APIResponse<>(200, file.get("downloadUrl").getAsString());
        }

        // Fall back to the default download URL
        final int fileId = file.get("id").getAsInt();
        return new APIResponse<>(200, JsonParser.parseString(DOWNLOAD_URL.formatted(assetId, fileId))
                .getAsJsonObject().get("data").getAsString());
    }

    /**
     * Query the API for the ID associated with the given resource class.
     */
    private APIResponse<Integer> resolveClassId(final DependencyInfo.ResourceClass resourceClass)
            throws IOException, InterruptedException {
        if (CLASS_ID_CACHE.containsKey(resourceClass)) {
            return new APIResponse<>(200, CLASS_ID_CACHE.get(resourceClass));
        }
        final APIResponse<String> response = fetchString(CATEGORIES_URL);
        if (response.statusCode() != 200) {
            return new APIResponse<>(response.statusCode(), -1);
        }
        final JsonArray classes = JsonParser.parseString(response.body())
                .getAsJsonObject().getAsJsonArray("data");
        for (final JsonElement element : classes) {
            final JsonObject clazz = element.getAsJsonObject();
            if (CONTENT_TYPE_MAP.get(resourceClass)
                    .equalsIgnoreCase(clazz.get("name")
                    .getAsString())) {
                final int id = clazz.get("id").getAsInt();
                CLASS_ID_CACHE.put(resourceClass, id);
                return new APIResponse<>(200, id);
            }
        }
        return new APIResponse<>(404, -1);
    }

    /**
     * Query the API for the asset ID associated with the given slug.
     */
    private APIResponse<Integer> resolveAssetId(final String slug, final DependencyInfo.ResourceClass resourceClass)
            throws IOException, InterruptedException, FileNotFoundException {
        final APIResponse<Integer> classIdResponse = resolveClassId(resourceClass);
        if (classIdResponse.statusCode() != 200) {
            return new APIResponse<>(404, -1);
        }
        final int classId = classIdResponse.body();
        final String encodedSlug = URLEncoder.encode(slug, StandardCharsets.UTF_8);
        final String url = SEARCH_URL.formatted(classId, encodedSlug);
        final APIResponse<String> response = fetchString(url);
        if (response.statusCode() != 200) {
            return new APIResponse<>(response.statusCode(), -1);
        }
        final JsonArray results = JsonParser.parseString(response.body())
                .getAsJsonObject().getAsJsonArray("data");
        if (results.isEmpty()) {
            throw new FileNotFoundException("No CurseForge project found for slug " + slug);
        }
        return new APIResponse<>(200, results.get(0)
                .getAsJsonObject().get("id").getAsInt());
    }

    private APIResponse<String> fetchString(final String url)
            throws IOException, InterruptedException {
        final HttpResponse<String> response = Constants.HTTP_CLIENT.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .headers(requestHeaders().entrySet().stream()
                                .flatMap(e -> Arrays.asList(e.getKey(), e.getValue()).stream())
                                .toArray(String[]::new))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 204 || response.statusCode() == 404) {
            return new APIResponse<>(404, "");
        }
        return new APIResponse<>(response.statusCode(), response.body());
    }
}
