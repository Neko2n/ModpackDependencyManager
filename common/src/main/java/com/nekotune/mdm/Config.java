package com.nekotune.mdm;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Since;
import com.nekotune.mdm.definition.DependencyInfo;

public final class Config {

    /**
     * Where the configuration file is located.
     */
    public static final Path FILE_PATH = Path.of("config",
            "dependencies." + Constants.MOD_ID + ".json");

    public static final Config INSTANCE = new Config();

    private static final Gson GSON = new GsonBuilder()
            .setVersion(1.0)
            .setPrettyPrinting()
            .create();

    @Since(1.0)
    public boolean production = false;

    @Since(1.0)
    public ArrayList<DependencySettings> resourcePacks = new ArrayList<>();

    @Since(1.0)
    public ArrayList<DependencySettings> dataPacks = new ArrayList<>();

    @Since(1.0)
    public boolean hideForced = true;

    @Since(1.0)
    public ArrayList<String> downloaded = new ArrayList<>();

    @Since(1.0)
    public boolean warnEnabled = true;

    /**
     * @see DependencyInfo.DownloadTarget
     */
    public static final class DependencySettings {

        @Since(1.0)
        public String slug = "";

        @Since(1.0)
        public ArrayList<String> mirrors = new ArrayList<>();

        @Since(1.0)
        public ArrayList<DependencyInfo.DownloadTarget.Host> hosts = new ArrayList<>();

        @Since(1.0)
        public DependencyInfo.Mode mode = DependencyInfo.Mode.FORCED;

        @Since(1.0)
        public DependencyInfo.Type type = DependencyInfo.Type.RESOURCE_PACK;
    }

    private Config() {
    }

    /**
     * Loads existing settings from the configuration file,
     * if it exists.
     */
    public void load() {
        final Config config = fromExisting().orElse(new Config());
        this.production = config.production;
        this.resourcePacks = config.resourcePacks;
        this.dataPacks = config.dataPacks;
        this.hideForced = config.hideForced;
        this.downloaded = config.downloaded;
        this.warnEnabled = config.warnEnabled;
    }

    /**
     * Saves the current settings to the configuration file.
     */
    public void save() {
        final String json = GSON.toJson(this);
        try {
            final Path parent = FILE_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(FILE_PATH.getParent());
            }
            Files.writeString(FILE_PATH, json);
            Constants.LOG.debug("[Config] Saving SUCCESS");
        } catch (final IOException e) {
            Constants.LOG.error("[Config] Saving FAILED: " + e.toString());
        }
    }

    private static Optional<Config> fromExisting() {
        if (!(Files.exists(FILE_PATH, LinkOption.NOFOLLOW_LINKS)
                && Files.isReadable(FILE_PATH))) {
            Constants.LOG.debug("[Config] No config file found at "
                    + FILE_PATH.toString()
                    + "; Using default settings");
            return Optional.empty();
        }
        try {
            final BufferedReader reader = Files.newBufferedReader(FILE_PATH);
            final Config config = GSON.fromJson(reader, Config.class);
            reader.close();
            sanitize(config);
            Constants.LOG.debug("[Config] Loading SUCCESS");
            return Optional.of(config);
        } catch (final IOException e) {
            Constants.LOG.error("[Config] Loading FAILED: " + e.toString());
            try {
                Path backupPath;
                long i = 1;
                while (true) {
                    backupPath = FILE_PATH.resolveSibling(
                            FILE_PATH.getFileName() + "-" + i + ".bak");
                    if (!Files.exists(backupPath, LinkOption.NOFOLLOW_LINKS)) {
                        break;
                    }
                    i++;
                }
                Files.copy(FILE_PATH, backupPath, StandardCopyOption.COPY_ATTRIBUTES);
                Constants.LOG.debug("[Config] Backup created at " + backupPath.toString());
            } catch (final IOException e2) {
                Constants.LOG.error("[Config] Backup FAILED: " + e2.toString());
            }
            return Optional.empty();
        }
    }

    private static void sanitize(final Config config) {
        config.downloaded = new ArrayList<>(config.downloaded.stream().distinct().toList());
        config.resourcePacks.removeIf(v -> config.downloaded.contains(v.slug));
        config.dataPacks.removeIf(v -> config.downloaded.contains(v.slug));
        config.resourcePacks = new ArrayList<>(config.resourcePacks.stream().distinct().toList());
        config.dataPacks = new ArrayList<>(config.dataPacks.stream().distinct().toList());
    }
}
