package com.nekotune.mdm.platform.services.config.spec;

public sealed interface IConfigEntry permits ConfigDecorator, ConfigValue {}
