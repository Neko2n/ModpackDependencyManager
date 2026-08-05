package dev.nekotune.mdm;

import java.net.http.HttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	public static final String MOD_ID = "mdm";
	public static final String MOD_NAME = "Modpack Dependency Manager";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final int TOKEN_COLOR = 0xFFF5AA42;

	/**
	 * The versions of Minecraft that this mod can run on.
	 */
	public static final String[] MC_VERSIONS = {
		"1.21.1"
	};

	public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
}