package com.nekotune.mdm.web;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class APITarget {

    protected static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private static final Map<String, APITarget> API_TARGETS = new HashMap<>();

    private final Map<String, ArrayList<CachedResponse>> cache = new ConcurrentHashMap<>();
    private final Duration TTL = Duration.ofHours(1);

    record CachedResponse(byte[] body, Instant expiresAt) {}

    public APITarget(final String path) {
        API_TARGETS.put(path, this);
    }

    /**
     * Retrieves a file from the specified API endpoint.
     *
     * @param path The API path to retrieve the file from. See {@link #API_TARGETS}.
     * @param slug The slug to look up the file by.
     * @param targetGameVersions The game versions to target.
     * @return The HTTP response containing the retrieved file.
     */
    public static final APIResponse<byte[]> GET(final String path,
            final String slug, final String resourceClass,
            final String[] targetGameVersions) throws
                    IOException, InterruptedException {
        final ResourceClass resourceClassEnum;
        try {
            resourceClassEnum = ResourceClass.fromKey(resourceClass);
        } catch (final IllegalArgumentException e) {
            return new APIResponse<>(400, ("Invalid resource class: " + resourceClass).getBytes());
        }
        if (!API_TARGETS.containsKey(path)) {
            return new APIResponse<>(404, ("No API target found for path: " + path).getBytes());
        }
        return API_TARGETS.get(path).getMemoized(slug, targetGameVersions, resourceClassEnum);
    }

    private final APIResponse<byte[]> getMemoized(final String slug,
            final String[] targetGameVersions, final ResourceClass resourceClass) throws
                    IOException, InterruptedException {
        final ArrayList<CachedResponse> cachedResponses =
                cache.computeIfAbsent(slug, k -> new ArrayList<>());
        for (final CachedResponse cachedResponse : cachedResponses) {
            if (Instant.now().isBefore(cachedResponse.expiresAt())) {
                return new APIResponse<>(200, cachedResponse.body());
            }
        }
        final APIResponse<byte[]> response = this.GET(slug, targetGameVersions, resourceClass);
        if (response.statusCode() == 200) {
            cachedResponses.add(new CachedResponse(response.body(), Instant.now().plus(TTL)));
        }
        return response;
    }

    /**
     * Retrieves a file from the API.
     *
     * @param slug The slug to look up the file by.
     * @param targetGameVersions The game versions to target.
     * @return The HTTP response containing the retrieved file.
     */
    protected abstract APIResponse<byte[]> GET(final String slug,
            final String[] targetGameVersions, final ResourceClass resourceClass) throws
                    IOException, InterruptedException;

    public static record APIResponse<T>(
            int statusCode,
            T body) {
    }

    protected static enum ResourceClass {
        RESOURCE_PACK,
        DATA_PACK;

        public static ResourceClass fromKey(final String key) throws
                IllegalArgumentException {
            for (final ResourceClass resourceClass : ResourceClass.values()) {
                if (resourceClass.toString().equalsIgnoreCase(key)) {
                    return resourceClass;
                }
            }
            throw new IllegalArgumentException("Unknown resource class: " + key);
        }

        public String key() {
            return name();
        }

        public String toString() {
            return key();
        }
    }
}
