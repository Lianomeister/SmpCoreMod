package com.smpcore.liam.config;

import com.google.gson.JsonSyntaxException;
import com.smpcore.liam.SmpCore;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
	private static final String FILE_NAME = "smpcore.json";

	private ConfigManager() {
	}

	public static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	public static SmpCoreConfig loadOrCreate() {
		Path path = configPath();

		if (Files.notExists(path)) {
			SmpCoreConfig config = new SmpCoreConfig();
			save(config);
			return config;
		}

		try {
			String raw = Files.readString(path, StandardCharsets.UTF_8);
			return ConfigJson.fromJson(raw);
		} catch (JsonSyntaxException e) {
			SmpCore.LOGGER.error("Invalid config JSON at {}", path.toAbsolutePath(), e);
			return new SmpCoreConfig();
		} catch (IOException e) {
			SmpCore.LOGGER.error("Failed reading config at {}", path.toAbsolutePath(), e);
			return new SmpCoreConfig();
		}
	}

	public static void save(SmpCoreConfig config) {
		Path path = configPath();
		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, ConfigJson.toJson(config), StandardCharsets.UTF_8);
		} catch (IOException e) {
			SmpCore.LOGGER.error("Failed writing config at {}", path.toAbsolutePath(), e);
		}
	}
}
