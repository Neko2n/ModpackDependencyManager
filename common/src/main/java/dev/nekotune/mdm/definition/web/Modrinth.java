package dev.nekotune.mdm.definition.web;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.definition.web.api.ModrinthAPI;
import dev.nekotune.mdm.definition.web.api.ModrinthAPI.*;
import dev.nekotune.mdm.definition.web.api.WebAPI;
import dev.nekotune.mdm.definition.web.api.WebAPI.APIResponse;
import net.minecraft.server.packs.PackType;

public final class Modrinth extends WebHost {

    public static final Modrinth INSTANCE = new Modrinth();

    private Modrinth() {
    }

    @Override
    protected APIResponse<Path> GET(final String slug,
            final PackType packType, final Path downloadTo) throws IOException, InterruptedException,
            SecurityException {
        final APIResponse<String> fileUrl = resolveFileURL(slug, packType);
        Constants.LOG.debug("[WebFetch$Modrinth] Queried file URL, got " + fileUrl.statusCode() + ": " + fileUrl.body());
        if (fileUrl.statusCode() != 200) {
            return new APIResponse<>(fileUrl.statusCode(), null);
        }
        return ModrinthAPI.Endpoints.download(fileUrl.body(), downloadTo);
    }

    private APIResponse<String> resolveFileURL(final String slug, final PackType packType)
            throws IOException, InterruptedException, SecurityException {

        // Fetch a list of version URLs
        final String url = ModrinthAPI.VERSIONS_URL.formatted(slug);
        final HttpResponse<String> response = fetchString(url);
        if (response.statusCode() != 200) {
            return new APIResponse<>(response.statusCode(), "");
        }
        final List<ModrinthAPI$version> versions = ModrinthAPI$versions.fromJson(response.body()).stream()
                // .filter(version -> version.requested_status() == ModrinthAPI$requested_status.listed)
                .filter(version -> versionLoadsFor(version, packType))
                .toList();
        if (versions.isEmpty()) {
            Constants.LOG.debug("[WebFetch$Modrinth] Versions came up empty for slug \"" + slug + "\" with pack type " + packType.toString());
            return new APIResponse<>(404, "");
        }

        // Return the first (newest) file for the running game version
        for (final ModrinthAPI$version version : versions) {
            if (!hasRunningGameVersion(version))
                continue;
            for (final ModrinthAPI$file file : version.files()) {
                return new APIResponse<>(200, file.url());
            }
        }
        Constants.LOG.debug("[WebFetch$Modrinth] File for minecraft "
                + Arrays.deepToString(Constants.MC_VERSIONS)
                + " not found at url "
                + url
                + "; using primary file.");

        // Return primary version
        for (final ModrinthAPI$file file : allFiles(versions, packType)) {
            if (file.primary()) {
                return new APIResponse<>(200, file.url());
            }
        }
        Constants.LOG.debug("[WebFetch$Modrinth] No primary file found at url "
                + url
                + "; using latest file.");

        // Return the first valid version
        for (final ModrinthAPI$file file : allFiles(versions, packType)) {
            return new APIResponse<>(200, file.url());
        }
        Constants.LOG.debug("[WebFetch$Modrinth] No files found at url " + url);

        // No valid versions found
        return new APIResponse<>(404, "");
    }

    private boolean versionLoadsFor(final ModrinthAPI$version version, final PackType packType) {
        for (final ModrinthAPI$loader loader : version.loaders()) {
            switch (loader) {
                case datapack:
                    if (packType == PackType.SERVER_DATA) {
                        return true;
                    }
                    break;
                case minecraft:
                    if (packType == PackType.CLIENT_RESOURCES) {
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    private boolean hasRunningGameVersion(final ModrinthAPI$version version) {
        for (final String gameVersion : version.game_versions()) {
            for (final String mcVersion : Constants.MC_VERSIONS) {
                if (gameVersion.equals(mcVersion))
                    return true;
            }
        }
        return false;
    }

    private List<ModrinthAPI$file> allFiles(final List<ModrinthAPI$version> versions, final PackType packType) {
        final List<ModrinthAPI$file> files = new ArrayList<>();
        for (final ModrinthAPI$version version : versions) {
            if (!versionLoadsFor(version, packType))
                continue;
            files.addAll(version.files());
        }
        return files;
    }

    private HttpResponse<String> fetchString(final String url)
            throws IOException, InterruptedException, SecurityException {
        final HttpResponse<String> response = WebAPI.HTTP_CLIENT.send(
                HttpRequest.newBuilder().uri(URI.create(url)).build(),
                HttpResponse.BodyHandlers.ofString());
        return response;
    }
}
