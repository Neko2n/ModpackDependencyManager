package dev.nekotune.mdm.definition.web.api;

import java.io.IOException;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.nekotune.mdm.definition.web.api.CurseforgeAPI.Responses.GET.mods.SearchResponse;
import net.minecraft.server.packs.PackType;

/**
 * A portion of the Curseforge developer API translated into Java.
 * This does not contain the full Curseforge API; it only contains what's needed
 * for the mod to work.
 */
public interface CurseforgeAPI extends WebAPI {

    public static final int MINECRAFT_GAME_ID = 432;
    public static final Map<PackType, Integer> PACK_TYPE_CLASS_IDS = new EnumMap<>(Map.of(
            PackType.CLIENT_RESOURCES, 12,
            PackType.SERVER_DATA, 6945));
    public static final Gson GSON = new GsonBuilder().create();

    // Just a tracking key, this isn't sensitive information
    public static final String API_KEY = "$2a$10$H0H1nUTWnyquwe63X/2BsuRahDATysk9ub4kI2KgVeuUppffsGLji";

    public static interface Urls {
        public static final String API = "https://api.curseforge.com/v1";
        public static final String SEARCH_BY_SLUG = API + "/mods/search?gameId=" + MINECRAFT_GAME_ID + "&slug=%s";
        // public static final String FILES = API + "/mods/%d/files";
        // public static final String DOWNLOAD = FILES + "/%d/download-url";
    }

    public static interface Endpoints {

        public static final String[] HEADERS = new String[] {
                "x-api-key", API_KEY,
                "Accept", "application/json"
        };

        public static interface GET {

            public static interface mods {

                public static APIResponse<SearchResponse> search(final String slug)
                        throws IOException, InterruptedException, SecurityException {
                    final String url = Urls.SEARCH_BY_SLUG.formatted(slug);
                    final APIResponse<String> response = WebAPI.request(url, HEADERS, BodyHandlers.ofString());
                    if (response.statusCode() != 200) {
                        return new APIResponse<>(response.statusCode(), null);
                    }
                    final SearchResponse searchResponse = GSON.fromJson(response.body(), SearchResponse.class);
                    return new APIResponse<>(200, searchResponse);
                }

                public static APIResponse<Path> download(final String fileUrl, final Path downloadTo)
                        throws IOException, InterruptedException, SecurityException {
                    return WebAPI.download(fileUrl, HEADERS, downloadTo);
                }
            }
        }
    }

    public static interface Responses {
        public static interface GET {
            public static interface mods {

                public static record DataResponse(
                        String data) {
                }

                public static record SearchResponse(
                        List<Mod> data) {
                }

                public static record Mod(
                        int id,
                        int gameId,
                        int classId,
                        int mainFileId,
                        String name,
                        String slug,
                        List<File> latestFiles,
                        boolean allowModDistribution,
                        boolean isAvailable) {

                    public boolean matches(final PackType packType) {
                        return this.classId == PACK_TYPE_CLASS_IDS.get(packType);
                    }
                }

                public static record File(
                        int id,
                        int gameId,
                        int modId,
                        boolean isAvailable,
                        String displayName,
                        String fileName,
                        FileReleaseType releaseType,
                        FileStatus fileStatus,
                        List<FileHash> hashes,
                        String downloadUrl,
                        List<String> gameVersions) {

                    public static record FileHash(
                            String value,
                            HashAlgo algo) {

                        public static enum HashAlgo {
                            Sha1,
                            Md5;
                        }
                    }

                    public static enum FileReleaseType {
                        Release,
                        Beta,
                        Alpha;
                    }

                    public static enum FileStatus {
                        Processing,
                        ChangesRequired,
                        UnderReview,
                        Approved,
                        Rejected,
                        MalwareDetected,
                        Deleted,
                        Archived,
                        Testing,
                        Released,
                        ReadyForReview,
                        Deprecated,
                        Baking,
                        AwaitingPublishing,
                        FailedPublishing,
                        Cooking,
                        Cooked,
                        UnderManualReview,
                        ScanningForMalware,
                        ProcessingFile,
                        PendingRelease,
                        ReadyForCooking,
                        PostProcessing;
                    }
                }
            }
        }
    }
}
