package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.IdUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class EnchantmentRulesFeature {
	private static boolean registered;

	private static volatile boolean clampOnJoin;
	private static volatile int tickInterval = 20 * 30;
	private static volatile Set<String> bannedEnchants = Set.of();
	private static volatile Map<String, Integer> limits = Map.of();

	private EnchantmentRulesFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);

		if (registered) {
			return;
		}
		registered = true;

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (!clampOnJoin) {
				return;
			}
			Set<String> currentBanned = bannedEnchants;
			Map<String, Integer> currentLimits = limits;
			if (currentBanned.isEmpty() && currentLimits.isEmpty()) {
				return;
			}

			Registry<Enchantment> enchantments = handler.player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			boolean changed = enforceOnPlayer(handler.player, enchantments, currentBanned, currentLimits);
			if (changed) {
				handler.player.containerMenu.broadcastChanges();
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(new ServerTickEvents.EndTick() {
			private int ticks;

			@Override
			public void onEndTick(MinecraftServer server) {
				int interval = tickInterval;
				Set<String> currentBanned = bannedEnchants;
				Map<String, Integer> currentLimits = limits;
				if (interval <= 0 || (currentBanned.isEmpty() && currentLimits.isEmpty())) {
					return;
				}

				ticks++;
				if (ticks < interval) {
					return;
				}
				ticks = 0;

				Registry<Enchantment> enchantments = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					boolean changed = enforceOnPlayer(player, enchantments, currentBanned, currentLimits);
					if (changed) {
						player.containerMenu.broadcastChanges();
					}
				}
			}
		});
	}

	public static void reload(SmpCoreConfig config) {
		clampOnJoin = config.enchantments.clampOnJoin;
		tickInterval = config.enchantments.scanSeconds <= 0 ? -1 : Math.max(1, config.enchantments.scanSeconds * 20);
		bannedEnchants = toNormalizedIdSet(config.enchantments.bannedEnchantments);
		limits = buildLimits(config);
	}

	private static Map<String, Integer> buildLimits(SmpCoreConfig config) {
		Map<String, Integer> limits = new HashMap<>();
		if (config.enchantments.limits.sharpnessMax > 0) {
			limits.put("minecraft:sharpness", config.enchantments.limits.sharpnessMax);
		}
		if (config.enchantments.limits.protectionMax > 0) {
			limits.put("minecraft:protection", config.enchantments.limits.protectionMax);
		}
		return limits;
	}

	private static boolean enforceOnPlayer(
			ServerPlayer player,
			Registry<Enchantment> enchantments,
			Set<String> bannedEnchants,
			Map<String, Integer> limits
	) {
		boolean changed = false;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack.isEmpty()) {
				continue;
			}

			if (enforceOnStack(stack, enchantments, bannedEnchants, limits)) {
				changed = true;
			}
		}
		return changed;
	}

	private static boolean enforceOnStack(
			ItemStack stack,
			Registry<Enchantment> enchantments,
			Set<String> bannedEnchants,
			Map<String, Integer> limits
	) {
		boolean changed = false;

		ItemEnchantments direct = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
		if (!direct.isEmpty()) {
			ItemEnchantments updated = enforceEnchantments(direct, enchantments, bannedEnchants, limits);
			if (!updated.equals(direct)) {
				stack.set(DataComponents.ENCHANTMENTS, updated);
				changed = true;
			}
		}

		ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
		if (!stored.isEmpty()) {
			ItemEnchantments updated = enforceEnchantments(stored, enchantments, bannedEnchants, limits);
			if (!updated.equals(stored)) {
				stack.set(DataComponents.STORED_ENCHANTMENTS, updated);
				changed = true;
			}
		}

		return changed;
	}

	private static ItemEnchantments enforceEnchantments(
			ItemEnchantments original,
			Registry<Enchantment> enchantments,
			Set<String> bannedEnchants,
			Map<String, Integer> limits
	) {
		ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(original);

		if (!bannedEnchants.isEmpty()) {
			mutable.removeIf(holder -> bannedEnchants.contains(enchantId(enchantments, holder)));
		}

		if (!limits.isEmpty()) {
			Set<net.minecraft.core.Holder<Enchantment>> keys = new HashSet<>(mutable.keySet());
			for (var holder : keys) {
				String id = enchantId(enchantments, holder);
				Integer max = limits.get(id);
				if (max == null) {
					continue;
				}

				int lvl = mutable.getLevel(holder);
				if (lvl > max) {
					mutable.set(holder, max);
				}
			}
		}

		return mutable.toImmutable();
	}

	private static String enchantId(Registry<Enchantment> enchantments, net.minecraft.core.Holder<Enchantment> holder) {
		Identifier id = enchantments.getKey(holder.value());
		return id == null ? "" : id.toString();
	}

	private static Set<String> toNormalizedIdSet(Iterable<String> ids) {
		Set<String> set = new HashSet<>();
		for (String raw : ids) {
			String normalized = normalizeId(raw);
			if (normalized != null) {
				set.add(normalized);
			}
		}
		return set;
	}

	private static String normalizeId(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		Identifier id = IdUtil.parse(raw.trim());
		return id == null ? null : id.toString();
	}
}
