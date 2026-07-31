package com.nekotune.mdm;

import java.util.function.Supplier;

import com.nekotune.mdm.definition.web.Curseforge;
import com.nekotune.mdm.definition.web.Modrinth;
import com.nekotune.mdm.definition.web.Website;

public enum Websites implements Supplier<Website> {
    CURSEFORGE(Curseforge.INSTANCE),
    MODRINTH(Modrinth.INSTANCE);

    private final Website websiteInstance;

    private Websites(final Website websiteInstance) {
        this.websiteInstance = websiteInstance;
    }

    @Override
    public Website get() {
        return websiteInstance;
    }
}
