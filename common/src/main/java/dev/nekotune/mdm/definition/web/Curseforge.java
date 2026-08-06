package dev.nekotune.mdm.definition.web;

import java.io.IOException;
import java.nio.file.Path;

import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.definition.web.api.CurseforgeAPI.*;
import dev.nekotune.mdm.definition.web.api.WebAPI.APIResponse;
import net.minecraft.server.packs.PackType;

public class Curseforge extends WebHost {

    public static final Curseforge INSTANCE = new Curseforge();

    @Override
    public APIResponse<Path> GET(final String slug, final PackType packType, final Path downloadTo)
            throws IOException, InterruptedException, SecurityException {
        final APIResponse<ModFileIdPair> fileId = resolveFileId(slug, packType);
        if (fileId.statusCode() != 200) {
            return new APIResponse<>(fileId.statusCode(), null);
        }
        return Endpoints.GET.mods.download(fileId.body().modId, fileId.body().fileId, downloadTo);
    }

    private APIResponse<ModFileIdPair> resolveFileId(final String slug, final PackType packType)
            throws IOException, InterruptedException, SecurityException {

        // Get the mod project data
        final var response = Endpoints.GET.mods.search(slug);
        if (response.statusCode() != 200) {
            return new APIResponse<>(response.statusCode(), null);
        }
        final var mods = response.body().data().stream()
                .filter(mod -> mod.matches(packType))
                .toList();
        if (mods.isEmpty()) {
            return new APIResponse<>(404, null);
        }
        final var mod = mods.getFirst(); // There's only ever going to be one
        if (!(mod.isAvailable() && mod.allowModDistribution())) {
            return new APIResponse<>(404, null);
        }

        // Filter for valid files
        final var files = mod.latestFiles().stream()
                .filter(Responses.GET.mods.File::isAvailable)
                .toList();
        if (files.isEmpty()) {
            return new APIResponse<>(404, null);
        }

        // Return the first (newest) file for the running game version
        final var matchesGameVersion = files.stream()
                .filter(file -> {
                    for (final String fileVersion : file.gameVersions()) {
                        for (final String mcVersion : Constants.MC_VERSIONS) {
                            if (fileVersion.matches(mcVersion))
                                return true;
                        }
                    }
                    return false;
                }).toList();
        if (!matchesGameVersion.isEmpty()) {
            return new APIResponse<>(200, new ModFileIdPair(mod.id(), matchesGameVersion.getFirst().id()));
        }

        // Fallback: return primary version
        return new APIResponse<>(200, new ModFileIdPair(mod.id(), mod.mainFileId()));
    }

    private static record ModFileIdPair(
            int modId,
            int fileId) {
    }
}
