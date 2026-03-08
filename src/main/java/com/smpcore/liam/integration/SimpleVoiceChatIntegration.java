package com.smpcore.liam.integration;

import com.smpcore.liam.SmpCore;
import com.smpcore.liam.config.SmpCoreConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public final class SimpleVoiceChatIntegration {
	private static final String MOD_ID = "voicechat";
	private static final String DIR_NAME = "voicechat";
	private static final String FILE_NAME = "voicechat-server.properties";

	private SimpleVoiceChatIntegration() {
	}

	public static boolean isInstalled() {
		return FabricLoader.getInstance().isModLoaded(MOD_ID);
	}

	public static Path serverConfigPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(DIR_NAME).resolve(FILE_NAME);
	}

	public static void syncFromVoiceChatConfigIfPresent(SmpCoreConfig config) {
		Path path = serverConfigPath();
		if (Files.notExists(path)) {
			return;
		}
		Properties props = load(path);
		if (props == null) {
			return;
		}

		SmpCoreConfig.VoiceChat vc = config.voiceChat;
		vc.port = getInt(props, "port", vc.port);
		vc.bindAddress = props.getProperty("bind_address", vc.bindAddress);
		vc.maxVoiceDistance = getDouble(props, "max_voice_distance", vc.maxVoiceDistance);
		vc.whisperDistance = getDouble(props, "whisper_distance", vc.whisperDistance);
		vc.enableGroups = getBool(props, "enable_groups", vc.enableGroups);
		vc.allowRecording = getBool(props, "allow_recording", vc.allowRecording);
		vc.spectatorInteraction = getBool(props, "spectator_interaction", vc.spectatorInteraction);
		vc.forceVoiceChat = getBool(props, "force_voice_chat", vc.forceVoiceChat);
		vc.loginTimeoutMs = getInt(props, "login_timeout", vc.loginTimeoutMs);

		String codec = props.getProperty("codec", null);
		if (codec != null) {
			vc.codec = parseCodec(codec, vc.codec);
		}
	}

	public static void applyToVoiceChatConfigIfEnabled(SmpCoreConfig config) {
		if (!config.voiceChat.manageSimpleVoiceChat) {
			return;
		}

		Path path = serverConfigPath();
		try {
			Files.createDirectories(path.getParent());
		} catch (IOException e) {
			SmpCore.LOGGER.warn("Failed creating voicechat config directory {}", path.getParent(), e);
			return;
		}

		Properties props = Files.exists(path) ? load(path) : new Properties();
		if (props == null) {
			props = new Properties();
		}

		SmpCoreConfig.VoiceChat vc = config.voiceChat;
		props.setProperty("port", Integer.toString(vc.port));
		props.setProperty("bind_address", vc.bindAddress == null ? "" : vc.bindAddress);
		props.setProperty("max_voice_distance", Double.toString(vc.maxVoiceDistance));
		props.setProperty("whisper_distance", Double.toString(vc.whisperDistance));
		props.setProperty("enable_groups", Boolean.toString(vc.enableGroups));
		props.setProperty("allow_recording", Boolean.toString(vc.allowRecording));
		props.setProperty("spectator_interaction", Boolean.toString(vc.spectatorInteraction));
		props.setProperty("force_voice_chat", Boolean.toString(vc.forceVoiceChat));
		props.setProperty("login_timeout", Integer.toString(vc.loginTimeoutMs));
		props.setProperty("codec", vc.codec.name().toLowerCase(Locale.ROOT));

		save(path, props);
	}

	private static Properties load(Path path) {
		Properties p = new Properties();
		try (InputStream in = Files.newInputStream(path)) {
			p.load(in);
			return p;
		} catch (IOException e) {
			SmpCore.LOGGER.warn("Failed reading Simple Voice Chat server config at {}", path.toAbsolutePath(), e);
			return null;
		}
	}

	private static void save(Path path, Properties props) {
		try (OutputStream out = Files.newOutputStream(path)) {
			props.store(out, "Managed by SMP Core");
		} catch (IOException e) {
			SmpCore.LOGGER.warn("Failed writing Simple Voice Chat server config at {}", path.toAbsolutePath(), e);
		}
	}

	private static boolean getBool(Properties props, String key, boolean fallback) {
		String raw = props.getProperty(key, null);
		if (raw == null) {
			return fallback;
		}
		return Boolean.parseBoolean(raw.trim());
	}

	private static int getInt(Properties props, String key, int fallback) {
		String raw = props.getProperty(key, null);
		if (raw == null) {
			return fallback;
		}
		try {
			return Integer.parseInt(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}

	private static double getDouble(Properties props, String key, double fallback) {
		String raw = props.getProperty(key, null);
		if (raw == null) {
			return fallback;
		}
		try {
			return Double.parseDouble(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}

	private static SmpCoreConfig.VoiceChatCodec parseCodec(String raw, SmpCoreConfig.VoiceChatCodec fallback) {
		String norm = raw.trim().toUpperCase(Locale.ROOT);
		return switch (norm) {
			case "VOIP" -> SmpCoreConfig.VoiceChatCodec.VOIP;
			case "AUDIO" -> SmpCoreConfig.VoiceChatCodec.AUDIO;
			case "RESTRICTED_LOWDELAY", "RESTRICTED-LOWDELAY", "RESTRICTEDLOWDELAY" -> SmpCoreConfig.VoiceChatCodec.RESTRICTED_LOWDELAY;
			default -> fallback;
		};
	}
}

