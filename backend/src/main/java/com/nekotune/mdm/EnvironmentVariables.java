package com.nekotune.mdm;

import java.util.function.Supplier;

public enum EnvironmentVariables implements Supplier<String> {
    
    CURSEFORGE_API_KEY;

    static {
        for (final EnvironmentVariables envVar : values()) {
            if (envVar.get() == null) {
                throw new IllegalStateException(
                        "Missing environment variable: " + envVar.name());
            }
        }
    }

    private String cached = "";

    @Override
    public String get() {
        if (cached.isEmpty()) {
            cached = System.getenv(name());
        }
        return cached;
    }
}
