package com.smpcore.liam.client.integration;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class SimpleVoiceChatClientConfig {
	private static final String MOD_ID = "voicechat";
	private static final String DIR_NAME = "voicechat";
	private static final String FILE_NAME = "voicechat-client.properties";

	private SimpleVoiceChatClientConfig() {
	}

	public static boolean isInstalled() {
		return FabricLoader.getInstance().isModLoaded(MOD_ID);
	}

	public static Path clientConfigPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(DIR_NAME).resolve(FILE_NAME);
	}

	public static Properties loadOrEmpty() {
		Path path = clientConfigPath();
		if (Files.notExists(path)) {
			return new Properties();
		}
		Properties p = new Properties();
		try (InputStream in = Files.newInputStream(path)) {
			p.load(in);
			return p;
		} catch (IOException e) {
			return new Properties();
		}
	}

	public static void save(Properties props) throws IOException {
		Path path = clientConfigPath();
		Files.createDirectories(path.getParent());
		try (OutputStream out = Files.newOutputStream(path)) {
			props.store(out, "Edited via SMP Core");
		}
	}
}

