package com.nekotune.mdm.definition;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import com.nekotune.mdm.Config;
import com.nekotune.mdm.Config.DependencySettings;
import com.nekotune.mdm.definition.web.Curseforge;
import com.nekotune.mdm.definition.web.Modrinth;
import com.nekotune.mdm.definition.web.WebHostAPI;
import com.nekotune.mdm.definition.web.WebHostAPI.ResourceClass;

public interface DependencyInfo {

    /**
     * An ordered list of dependency slugs determining the order in which
     * said dependencies should be loaded into the game.
     * Populated by {@link Config}.
     */
    public static final LinkedList<String> LOAD_ORDER = new LinkedList<>();

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

        public boolean matches(final DownloadTarget target) {
            return target.mode == this;
        }
    }

    /**
     * Information about a dependency download target.
     * 
     * @param slug    The slug to search up and download from.
     * @param mirrors Mirror slugs for if the initial slug failed to find a match.
     * @param hosts   The website host(s) to attempt to download from.
     * @param mode    The way the dependency should be loaded into the game once
     *                downloaded.
     * @param type    Whether the dependency is a resource pack or a data pack.
     */
    public record DownloadTarget(
            String slug,
            List<String> mirrors,
            List<DownloadTarget.Host> hosts,
            Mode mode,
            ResourceClass type) {
        
        public DownloadTarget(final DependencySettings settings) {
            this(settings.slug, settings.mirrors, settings.hosts, settings.mode, settings.type);
        }

        @Override
        public String toString() {
            return "{slug: " + slug
                    + ", mirror: " + mirrors.toString()
                    + ", host: " + hosts.toString()
                    + ", mode: " + mode.toString()
                    + ", type: " + type.toString()
                    + "}";
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
        
            public boolean matches(final DependencyInfo.DownloadTarget target) {
                return target.hosts().contains(this);
            }
        
            @Override
            public WebHostAPI get() {
                return website;
            }
        }
    }
}