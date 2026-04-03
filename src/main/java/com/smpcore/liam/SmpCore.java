package com.smpcore.liam;

import com.smpcore.liam.config.ConfigManager;
import com.smpcore.liam.config.ConfigJson;
import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.feature.SmpCoreFeatures;
import com.smpcore.liam.integration.SimpleVoiceChatIntegration;
import com.smpcore.liam.item.SmpCoreItems;
import com.smpcore.liam.net.SmpCorePayloads;
import com.smpcore.liam.util.IdUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.storage.LevelResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SmpCore implements ModInitializer {
	public static final String MOD_ID = "smpcore";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static volatile SmpCoreConfig config;

	@Override
	public void onInitialize() {
		SmpCoreItems.registerAll();
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
		SimpleVoiceChatIntegration.applyToVoiceChatConfigIfEnabled(newConfig);
		SmpCoreFeatures.reloadAll(newConfig);
	}

	private static void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			var smpcoreRoot = Commands.literal("smpcore")
					.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
					.executes(ctx -> {
						ServerPlayer player = ctx.getSource().getPlayerOrException();
						openAdminScreen(player);
						return 1;
					});

			smpcoreRoot.then(Commands.literal("recipes")
					.then(Commands.literal("status").executes(ctx -> {
						SmpCoreConfig cfg = getConfig();
						int lines = cfg.recipes.shapeless == null ? 0 : cfg.recipes.shapeless.size();
						ctx.getSource().sendSuccess(() -> Component.literal("Custom recipes: " + (cfg.recipes.enabled ? "enabled" : "disabled") + " (" + lines + " entries)"), false);
						return 1;
					}))
					.then(Commands.literal("install").executes(ctx -> {
						if (!getConfig().recipes.enabled) {
							ctx.getSource().sendFailure(Component.literal("Custom recipes are disabled in config. Enable them in the SMP Core UI first."));
							return 0;
						}
						MinecraftServer server = ctx.getSource().getServer();
						int written = installRecipeDatapack(server, getConfig(), ctx.getSource().getTextName());
						ctx.getSource().sendSuccess(() -> Component.literal("Wrote " + written + " recipe(s) to datapack. Run /reload to apply."), false);
						return 1;
					})));

			dispatcher.register(smpcoreRoot);

			dispatcher.register(Commands.literal("smpstart")
					.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
					.then(Commands.literal("grace")
							.then(Commands.argument("seconds", IntegerArgumentType.integer(0, 86400))
									.executes(ctx -> {
										int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
										long endsAt = seconds <= 0 ? 0L : System.currentTimeMillis() + (long) seconds * 1000L;

										updateConfig(c -> {
											c.start.graceEndsAtEpochMillis = endsAt;
											// Force PvP off while grace is active.
											c.gameplay.pvpEnabled = seconds <= 0;
										});

										if (getConfig().start.announceGrace) {
											if (seconds <= 0) {
												ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(
														Component.literal("Grace period ended. PvP is now enabled."),
														false
												);
											} else {
												ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(
														Component.literal("Grace period started for " + seconds + "s. PvP is disabled."),
														false
												);
											}
										}
										return 1;
									})))
					.then(Commands.literal("status").executes(ctx -> {
						long remaining = com.smpcore.liam.feature.GraceFeature.remainingSeconds();
						if (remaining <= 0) {
							ctx.getSource().sendSuccess(() -> Component.literal("No grace period active."), false);
						} else {
							ctx.getSource().sendSuccess(() -> Component.literal("Grace remaining: " + remaining + "s (PvP disabled)."), false);
						}
						return 1;
					})));
		});
	}

	private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

	private static int installRecipeDatapack(MinecraftServer server, SmpCoreConfig config, String requestedBy) {
		Path datapacks = server.getWorldPath(LevelResource.DATAPACK_DIR);
		Path packDir = datapacks.resolve("smpcore-custom-recipes");
		Path recipeDir = packDir.resolve("data").resolve(MOD_ID).resolve("recipe");

		try {
			Files.createDirectories(recipeDir);

			JsonObject mcmeta = new JsonObject();
			JsonObject pack = new JsonObject();
			pack.addProperty("pack_format", SharedConstants.DATA_PACK_FORMAT_MAJOR);
			pack.addProperty("description", "SMP Core Custom Recipes (generated)");
			mcmeta.add("pack", pack);
			Files.writeString(packDir.resolve("pack.mcmeta"), PRETTY_GSON.toJson(mcmeta), StandardCharsets.UTF_8);

			List<String> lines = config.recipes.shapeless == null ? List.of() : config.recipes.shapeless;
			int written = 0;
			int idx = 0;
			for (String line : lines) {
				if (line == null || line.isBlank() || line.trim().startsWith("#")) {
					continue;
				}
				RecipeSpec spec = parseShapeless(line.trim());
				if (spec == null) {
					continue;
				}

				JsonObject recipe = new JsonObject();
				recipe.addProperty("type", "minecraft:crafting_shapeless");
				JsonArray ingredients = new JsonArray();
				for (int i = 0; i < spec.ingredients.size(); i++) {
					ingredients.add(spec.ingredients.get(i));
				}
				recipe.add("ingredients", ingredients);
				JsonObject result = new JsonObject();
				result.addProperty("count", spec.outputCount);
				result.addProperty("id", spec.outputId);
				recipe.add("result", result);

				String file = "custom_" + idx + "_" + safeFilePart(spec.outputId) + ".json";
				Files.writeString(recipeDir.resolve(file), PRETTY_GSON.toJson(recipe), StandardCharsets.UTF_8);
				idx++;
				written++;
			}

			Files.writeString(packDir.resolve("smpcore-recipes.txt"),
					"Generated by SMP Core for " + server.getMotd() + "\n" +
							"Requested by: " + requestedBy + "\n" +
							"Recipes: " + written + "\n",
					StandardCharsets.UTF_8);
			return written;
		} catch (Exception e) {
			LOGGER.warn("Failed writing recipe datapack", e);
			return 0;
		}
	}

	private static RecipeSpec parseShapeless(String line) {
		// <output_id> <count> <- <input_id> <count>, <input_id> <count>
		String[] parts = line.split("<-");
		if (parts.length != 2) {
			return null;
		}
		String left = parts[0].trim();
		String right = parts[1].trim();
		if (left.isEmpty() || right.isEmpty()) {
			return null;
		}

		String[] outParts = left.split("\\s+");
		if (outParts.length < 1) {
			return null;
		}
		String outputId = outParts[0].trim();
		if (IdUtil.parse(outputId) == null) {
			return null;
		}
		int outputCount = 1;
		if (outParts.length >= 2) {
			try {
				outputCount = Math.max(1, Integer.parseInt(outParts[1].trim()));
			} catch (Exception ignored) {
			}
		}

		ArrayList<String> ingredients = new ArrayList<>();
		for (String token : right.split(",")) {
			String t = token.trim();
			if (t.isEmpty()) {
				continue;
			}
			String[] inParts = t.split("\\s+");
			if (inParts.length < 1) {
				continue;
			}
			String inputId = inParts[0].trim();
			if (IdUtil.parse(inputId) == null) {
				return null;
			}
			int count = 1;
			if (inParts.length >= 2) {
				try {
					count = Math.max(1, Integer.parseInt(inParts[1].trim()));
				} catch (Exception ignored) {
				}
			}
			for (int i = 0; i < count; i++) {
				ingredients.add(inputId);
			}
		}
		if (ingredients.isEmpty()) {
			return null;
		}

		return new RecipeSpec(outputId, outputCount, ingredients);
	}

	private static String safeFilePart(String id) {
		String s = id.toLowerCase();
		s = s.replace(':', '_');
		s = s.replaceAll("[^a-z0-9_\\-\\.]+", "_");
		return s.length() > 48 ? s.substring(0, 48) : s;
	}

	private record RecipeSpec(String outputId, int outputCount, List<String> ingredients) {
	}

	private static void registerNetworking() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			server.execute(() -> {
				ServerPlayNetworking.send(handler.player, new SmpCorePayloads.AdminStatusPayload(handler.player.permissions().hasPermission(Permissions.COMMANDS_ADMIN)));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(SmpCorePayloads.RequestOpenAdminPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayer player = context.player();
				if (!player.permissions().hasPermission(Permissions.COMMANDS_ADMIN)) {
					return;
				}
				openAdminScreen(player);
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(SmpCorePayloads.SaveConfigPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayer player = context.player();
				if (!player.permissions().hasPermission(Permissions.COMMANDS_ADMIN)) {
					return;
				}
				try {
					SmpCoreConfig newConfig = ConfigJson.fromJson(payload.configJson());
					applyConfig(newConfig);
					MinecraftServer server = player.level().getServer();
					if (server != null) {
						broadcastConfigUpdate(server);
					}
				} catch (Exception e) {
					LOGGER.warn("Rejected config update from {}: invalid JSON", player.getName().getString(), e);
				}
			});
		});
	}

	private static void openAdminScreen(ServerPlayer player) {
		ServerPlayNetworking.send(player, new SmpCorePayloads.OpenAdminPayload(snapshotConfigJson()));
	}

	private static String snapshotConfigJson() {
		SmpCoreConfig snapshot = ConfigJson.fromJson(ConfigJson.toJson(getConfig()));
		SimpleVoiceChatIntegration.syncFromVoiceChatConfigIfPresent(snapshot);
		return ConfigJson.toJson(snapshot);
	}

	private static void broadcastConfigUpdate(MinecraftServer server) {
		String configJson = snapshotConfigJson();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.permissions().hasPermission(Permissions.COMMANDS_ADMIN)) {
				ServerPlayNetworking.send(player, new SmpCorePayloads.ConfigUpdatedPayload(configJson));
			}
		}
	}
}
