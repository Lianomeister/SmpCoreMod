package com.smpcore.liam;

import com.smpcore.liam.config.ConfigManager;
import com.smpcore.liam.config.ConfigJson;
import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.feature.SmpCoreFeatures;
import com.smpcore.liam.gui.AdminMenu;
import com.smpcore.liam.net.SmpCorePayloads;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmpCore implements ModInitializer {
	public static final String MOD_ID = "smpcore";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static volatile SmpCoreConfig config;

	@Override
	public void onInitialize() {
		config = ConfigManager.loadOrCreate();
		SmpCoreFeatures.initAll(config);
		SmpCorePayloads.register();
		registerCommands();
		registerNetworking();
		LOGGER.info("SMP Core initialized (configVersion={})", config.configVersion);
	}

	public static SmpCoreConfig getConfig() {
		return config;
	}

	public static synchronized void updateConfig(java.util.function.Consumer<SmpCoreConfig> mutator) {
		try {
			SmpCoreConfig copy = ConfigJson.fromJson(ConfigJson.toJson(config));
			mutator.accept(copy);
			applyConfig(copy);
		} catch (Exception e) {
			LOGGER.warn("Failed to update config", e);
		}
	}

	private static void applyConfig(SmpCoreConfig newConfig) {
		config = newConfig;
		ConfigManager.save(newConfig);
		SmpCoreFeatures.reloadAll(newConfig);
	}

	private static void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("smpcore")
					.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
					.executes(ctx -> {
						ServerPlayer player = ctx.getSource().getPlayerOrException();
						AdminMenu.openMain(player);
						return 1;
					}));
		});
	}

	private static void registerNetworking() {
		ServerPlayNetworking.registerGlobalReceiver(SmpCorePayloads.SaveConfigPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayer player = context.player();
				if (!player.permissions().hasPermission(Permissions.COMMANDS_ADMIN)) {
					return;
				}
				try {
					SmpCoreConfig newConfig = ConfigJson.fromJson(payload.configJson());
					applyConfig(newConfig);
				} catch (Exception e) {
					LOGGER.warn("Rejected config update from {}: invalid JSON", player.getName().getString(), e);
				}
			});
		});
	}

	private static void openAdminScreen(ServerPlayer player) {
		ServerPlayNetworking.send(player, new SmpCorePayloads.OpenAdminPayload(ConfigJson.toJson(getConfig())));
	}
}
