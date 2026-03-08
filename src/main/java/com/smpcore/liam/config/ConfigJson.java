package com.smpcore.liam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public final class ConfigJson {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private ConfigJson() {
	}

	public static String toJson(SmpCoreConfig config) {
		return GSON.toJson(config);
	}

	public static SmpCoreConfig fromJson(String raw) throws JsonSyntaxException {
		SmpCoreConfig config = GSON.fromJson(raw, SmpCoreConfig.class);
		return config == null ? new SmpCoreConfig() : config;
	}
}

