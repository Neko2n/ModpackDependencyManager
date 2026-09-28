package dev.nekotune.staple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Constants {

	public static final String MOD_ID = "staple";
	public static final String MOD_NAME = "Modpack Dependency Manager";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	/**
	 * The versions of Minecraft that this mod can run on.
	 */
	public static final String[] MC_VERSIONS = {
		"1.21.1"
	};
}