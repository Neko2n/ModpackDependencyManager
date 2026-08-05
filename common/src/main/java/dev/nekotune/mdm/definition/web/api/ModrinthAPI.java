package dev.nekotune.mdm.definition.web.api;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class ModrinthAPI {

    public static final String VERSIONS_URL = "https://api.modrinth.com/v2/project/%s/version";
    
    private static final Gson GSON = new GsonBuilder().create();

    public static class ModrinthAPI$versions extends HashSet<ModrinthAPI$version> {
        
        public static ModrinthAPI$versions fromJson(final String json) {
            return GSON.fromJson(json, ModrinthAPI$versions.class);
        }
    }

    public static record ModrinthAPI$version(
            String name,
            String version_number,
            Set<ModrinthAPI$dependency> dependencies,
            Set<String> game_versions,
            ModrinthAPI$version_type version_type,
            Set<ModrinthAPI$loader> loaders,
            boolean featured,
            ModrinthAPI$status status,
            ModrinthAPI$requested_status requested_status,
            String id,
            String project_id,
            String author_id,
            String date_published,
            int downloads,
            String changelog_url,
            Set<ModrinthAPI$file> files) {
        
        public ModrinthAPI$version {
            loaders = new HashSet<>(loaders.stream().filter(v -> v != null).toList());
        }
    }

    public static record ModrinthAPI$dependency(
            String version_id,
            String project_id,
            String file_name,
            ModrinthAPI$dependency_type dependency_type) {
    }

    public static record ModrinthAPI$file(
            ModrinthAPI$file$hashes hashes,
            String url,
            String filename,
            boolean primary,
            int size,
            ModrinthAPI$file$file_type file_type) {
        
        public ModrinthAPI$file {
            file_type = file_type != null ? file_type : ModrinthAPI$file$file_type.unknown;
        }
    }

    public static record ModrinthAPI$file$hashes(
            String sha512,
            String sha1) {
    }

    public static enum ModrinthAPI$file$file_type {

        @SerializedName("required-resource-pack")
        required_resource_pack,
        
        @SerializedName("optional-resource-pack")
        optional_resource_pack,
        
        @SerializedName("sources-jar")
        sources_jar,

        @SerializedName("dev-jar")
        dev_jar,

        @SerializedName("javadoc-jar")
        javadoc_jar,
        
        unknown,
        signature;
    }

    public static enum ModrinthAPI$loader {
        datapack,
        minecraft,
        fabric,
        forge,
        neoforge,
        quilt;
    }

    public static enum ModrinthAPI$version_type {
        release,
        beta,
        alpha;
    }

    public static enum ModrinthAPI$status {
        listed,
        archived,
        draft,
        unlisted,
        scheduled,
        unknown;
    }

    public static enum ModrinthAPI$requested_status {
        listed,
        archived,
        draft,
        unlisted;
    }

    public static enum ModrinthAPI$dependency_type {
        required,
        optional,
        incompatible,
        embedded;
    }
}
