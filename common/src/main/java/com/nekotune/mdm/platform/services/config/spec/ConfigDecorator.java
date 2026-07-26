package com.nekotune.mdm.platform.services.config.spec;

public final class ConfigDecorator implements IConfigEntry {
    
    public final String comment;

    private ConfigDecorator(String comment) {
        this.comment = comment;
    }

    public static ConfigDecorator comment(final String comment) {
        return new ConfigDecorator(comment);
    }
}
