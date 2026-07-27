package com.nekotune.mdm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	public static final String MOD_ID = "mdm";
	public static final String MOD_NAME = "Modpack Dependency Manager";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final String CONFIG_FILE_NAME = "dependencies." + Constants.MOD_ID + ".toml";
}