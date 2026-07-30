package com.nekotune.mdm;

import java.nio.file.Path;
import java.util.Optional;

import com.nekotune.mdm.web.Modrinth;

public class CommonClass {

    public static void init() {
        
        final Optional<Path> downloaded = Modrinth.INSTANCE.downloadAssets("fresh-animations");
        downloaded.ifPresentOrElse(path -> {
            Constants.LOG.debug("Successfully downloaded resource pack to file: " + path.toString());
        }, () -> {
            Constants.LOG.debug("Failed to download resource pack");
        });
    }
}