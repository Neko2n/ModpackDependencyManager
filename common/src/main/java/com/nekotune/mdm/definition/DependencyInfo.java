package com.nekotune.mdm.definition;

import java.util.List;
import java.util.function.Supplier;

import com.nekotune.mdm.definition.web.Curseforge;
import com.nekotune.mdm.definition.web.Modrinth;
import com.nekotune.mdm.definition.web.WebHostAPI;

/**
 * Information about a downloaded dependency.
 * 
 * @param slug         The slug to search up and download from.
 * @param mirrors      Mirror slugs for if the initial slug failed to find a
 *                     match.
 * @param hosts        The website host(s) to attempt to download from.
 * @param mode         The way the dependency should be loaded into the game
 *                     once
 *                     downloaded.
 * @param type         Whether the dependency is a resource pack or a data pack.
 * @param loadPriority Determines the order in which dependencies are loaded.
 */
public record DependencyInfo(
        String slug,
        List<String> mirrors,
        List<DependencyInfo.Host> hosts,
        Mode mode,
        ResourceClass type,
        int loadPriority) {

    @Override
    public String toString() {
        return "{slug: " + slug
                + ", mirror: " + mirrors.toString()
                + ", host: " + hosts.toString()
                + ", mode: " + mode.toString()
                + ", type: " + type.toString()
                + "}";
    }

    /**
     * @return The file name of the dependency when downloaded as a file.
     */
    public String fileName() {
        return "modpack." + slug + ".zip";
    }

    /**
     * @return The pack ID representing the dependency.
     */
    public String packId() {
        return "file/" + fileName();
    }

    /**
     * @return A formatted, readable string to represent the dependency.
     */
    public String title() {
        final StringBuilder sb = new StringBuilder(slug.toLowerCase()
                .replaceAll("[\\-_]", " "));
        boolean capitalize = true;
        for (int i = 0; i < sb.length(); i++) {
            final char c = sb.charAt(i);
            if (Character.isWhitespace(c)) {
                capitalize = true;
            } else if (capitalize) {
                sb.setCharAt(i, Character.toUpperCase(c));
                capitalize = false;
            }
        }
        return sb.toString();
    }

    public static enum Host implements Supplier<WebHostAPI> {
        CURSEFORGE(Curseforge.INSTANCE),
        MODRINTH(Modrinth.INSTANCE);

        public static List<Host> any() {
            return List.of(Host.values());
        }

        private final WebHostAPI website;

        private Host(final WebHostAPI website) {
            this.website = website;
        }

        public boolean matches(final DependencyInfo target) {
            return target.hosts().contains(this);
        }

        @Override
        public WebHostAPI get() {
            return website;
        }
    }

    /**
     * How this dependency should be loaded into the game.
     */
    public static enum Mode {
        /**
         * The dependency will be force-enabled and hidden.
         * Use this for mandatory game assets.
         * All dependencies marked FORCED will be loaded before all other dependencies
         * and resources,
         * so that they may be overridden.
         */
        FORCED(true),

        /**
         * The dependency will be enabled by default, but users can disable it.
         */
        OPTIONAL_ENABLED(false),

        /**
         * The dependency will be disabled by default, but users can enable it.
         */
        OPTIONAL_DISABLED(false),

        /**
         * The dependency will be downloaded, but will not be added to the game.
         * Use this for when you need to distribute specific assets,
         * modified or otherwise, and still need to support the author.
         */
        SUPPORT(true);

        public final boolean isHidden;

        private Mode(final boolean isHidden) {
            this.isHidden = isHidden;
        }

        public boolean matches(final DependencyInfo target) {
            return target.mode == this;
        }
    }

    public static enum ResourceClass {
        RESOURCE_PACK("resourcepacks"),
        DATA_PACK("datapacks");

        private ResourceClass(final String folder) {
            this.folder = folder;
        }

        public final String folder;
    }
}