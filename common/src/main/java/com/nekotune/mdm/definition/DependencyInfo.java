package com.nekotune.mdm.definition;

import java.util.LinkedList;
import java.util.List;

import com.nekotune.mdm.ConfigHandler;
import com.nekotune.mdm.definition.web.Curseforge;
import com.nekotune.mdm.definition.web.Modrinth;

public interface DependencyInfo {

    /**
     * An ordered list of dependency slugs determining the order in which
     * said dependencies should be loaded into the game.
     * Populated by {@link ConfigHandler}.
     */
    public static final LinkedList<String> LOAD_ORDER = new LinkedList<>();

    /**
     * Which host(s) a dependency should attempt to download from.
     */
    public static enum Host {
        /**
         * Only attempt to download the dependency from {@link Modrinth}
         */
        MODRINTH,

        /**
         * Only attempt to download the dependency from {@link Curseforge}
         */
        CURSEFORGE,

        /**
         * Attempts to download from all possible hosts, retrying the next host on
         * failure.
         */
        ANY;

        public boolean matches(final DownloadTarget target) {
            return target.host == this || target.host == ANY;
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
        FORCED,

        /**
         * The dependency will be enabled by default, but users can disable it.
         */
        OPTIONAL_ENABLED,

        /**
         * The dependency will be disabled by default, but users can enable it.
         */
        OPTIONAL_DISABLED,

        /**
         * The dependency will be downloaded, but will not be added to the game.
         * Use this for when you need to distribute specific assets,
         * modified or otherwise, and still need to support the author.
         */
        SUPPORT;

        public boolean matches(final DownloadTarget target) {
            return target.mode == this;
        }
    }

    /**
     * Whether the dependency is a resource pack or a data pack.
     */
    public static enum Type {
        RESOURCE_PACK,
        DATA_PACK;

        public boolean matches(final DownloadTarget target) {
            return target.type == this;
        }
    }

    /**
     * Information about a dependency download target.
     * 
     * @param slug    The slug to search up and download from.
     * @param mirrors Mirror slugs for if the initial slug failed to find a match.
     * @param host    The host(s) to download from.
     * @param mode    The way the dependency should be loaded into the game once
     *                downloaded.
     * @param type    Whether the dependency is a resource pack or a data pack.
     */
    public record DownloadTarget(
            String slug,
            List<String> mirrors,
            Host host,
            Mode mode,
            Type type) {

        @Override
        public String toString() {
            String mirrorStr = "[";
            for (final String mirrorSlug : mirrors) {
                mirrorStr += mirrorSlug + ", ";
            }
            mirrorStr = mirrorStr.substring(0, mirrorStr.length() - 2)
                    + "]";
            return "{slug: " + slug
                    + ", mirror: " + mirrorStr
                    + ", host: " + host.toString()
                    + ", mode: " + mode.toString()
                    + ", type: " + type.toString()
                    + "}";
        }
    }
}