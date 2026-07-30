package com.nekotune.mdm.web;

import java.io.IOException;
import java.net.HttpRetryException;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.nekotune.mdm.Constants;

public final class Curseforge extends WebFetch {

    public static final Curseforge INSTANCE = new Curseforge();

    private Curseforge() {
    }

    private static final int MINECRAFT_GAME_ID = 432;
    private static final String API_URL = "https://api.curseforge.com/v1";
    private static final String CATEGORIES_URL = API_URL + "/categories?gameId=" + MINECRAFT_GAME_ID
            + "&classesOnly=true";
    private static final String SEARCH_URL = API_URL + "/mods/search?gameId=" + MINECRAFT_GAME_ID + "&slug=%s";
    private static final String FILES_URL = API_URL + "/mods/%d/files";
    private static final String DOWNLOAD_URL = FILES_URL + "/%d/download-url";

    private static final String API_KEY = ""; // TODO: Get API key

    private static enum ResourceClass {
        RESOURCE_PACK("Resource Packs"),
        DATA_PACK("Data Packs");

        private final String str;

        private ResourceClass(final String str) {
            this.str = str;
        }

        public volatile Optional<Integer> id = Optional.empty();

        public String toString() {
            return str;
        }
    }

    @Override
    protected Map<String, String> requestHeaders() {
        return Map.of(
                "x-api-key", API_KEY,
                "Accept", "application/json");
    }

    @Override
    protected String resolveFileURL(final String slug)
            throws IOException, InterruptedException, SecurityException, HttpRetryException {

        // Fetch a list of files for the given mod slug
        final int modId = resolveModId(slug);
        final String url = FILES_URL.formatted(modId);
        final HttpResponse<String> response = fetchString(url);
        final JsonArray files = JsonParser.parseString(response.body())
                .getAsJsonObject()
                .getAsJsonArray("data");
        if (files.isEmpty()) {
            throw new HttpRetryException("No files found for project " + url, response.statusCode());
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
                for (final String mcVersion : Constants.MC_VERSIONS) {
                    if (gameVersion.getAsString().equals(mcVersion)) {
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
            Constants.LOG.debug("[WebFetch] Version for minecraft "
                    + Arrays.deepToString(Constants.MC_VERSIONS)
                    + " not found for CurseForge project " + slug
                    + "; using newest available file.");
            for (final JsonElement fileElement : files) {
                final JsonObject file = fileElement.getAsJsonObject();
                if (file.get("isAvailable").getAsBoolean()) {
                    chosen = Optional.of(file);
                    break;
                }
            }
        }

        final JsonObject file = chosen.orElseThrow(() -> {
            // No files exist for the given project slug
            return new HttpRetryException("No available (distributable) files found for CurseForge project " + slug,
                    response.statusCode());
        });

        // Use download URL if author has enabled third-party downloads
        if (file.has("downloadUrl")) {
            return file.get("downloadUrl").getAsString();
        }

        // Fall back to the default download URL
        final int fileId = file.get("id").getAsInt();
        try {
            return JsonParser.parseString(DOWNLOAD_URL.formatted(modId, fileId))
                    .getAsJsonObject().get("data").getAsString();
        } catch (final JsonSyntaxException e) {
            throw new HttpRetryException("Failed to resolve URL for file " + fileId + ": " + e.toString(),
                    response.statusCode());
        }
    }

    /**
     * Query the API for the ID associated with the given resource class.
     */
    private int resolveClassId(final ResourceClass resourceClass)
            throws IOException, InterruptedException, SecurityException, HttpRetryException {
        if (resourceClass.id.isPresent()) {
            return resourceClass.id.get();
        }
        final HttpResponse<String> response = fetchString(CATEGORIES_URL);
        final JsonArray classes = JsonParser.parseString(response.body())
                .getAsJsonObject().getAsJsonArray("data");
        for (final JsonElement element : classes) {
            final JsonObject clazz = element.getAsJsonObject();
            if (resourceClass.toString().equalsIgnoreCase(clazz.get("name").getAsString())) {
                final int id = clazz.get("id").getAsInt();
                ResourceClass.RESOURCE_PACK.id = Optional.of(id);
                return id;
            }
        }
        throw new HttpRetryException("Could not resolve CurseForge classId for " + resourceClass.toString(),
                response.statusCode());
    }

    /**
     * Query the API for the mod ID associated with the given slug.
     */
    private int resolveModId(final String slug)
            throws IOException, InterruptedException, SecurityException, HttpRetryException {
        final int classId = resolveClassId(ResourceClass.RESOURCE_PACK);
        final String encodedSlug = URLEncoder.encode(slug, StandardCharsets.UTF_8);
        final String url = SEARCH_URL.formatted(classId, encodedSlug);
        final HttpResponse<String> response = fetchString(url);
        final JsonArray results = JsonParser.parseString(response.body())
                .getAsJsonObject().getAsJsonArray("data");
        if (results.isEmpty()) {
            throw new HttpRetryException("No CurseForge project found for slug " + slug, response.statusCode());
        }
        return results.get(0).getAsJsonObject().get("id").getAsInt();
    }
}