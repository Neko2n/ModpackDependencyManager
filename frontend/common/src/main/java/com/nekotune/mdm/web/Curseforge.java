package com.nekotune.mdm.web;

public final class Curseforge extends BackendProxyAPI {

    public static final Curseforge INSTANCE = new Curseforge();

    private Curseforge() {
        super("curseforge");
    }
}