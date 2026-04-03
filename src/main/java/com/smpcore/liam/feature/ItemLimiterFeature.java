package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.IdUtil;
import com.smpcore.liam.util.NoticeCooldowns;
import com.smpcore.liam.util.TextUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ItemLimiterFeature {
	private static final NoticeCooldowns NOTICE_COOLDOWNS = new NoticeCooldowns();
	private static boolean registered;

	private static volatile boolean enabled;
	private static volatile boolean preventPickup;
	private static volatile boolean dropExcess;
	private static volatile boolean actionBar;
	private static volatile long minMillisBetweenNotices;
	private static volatile int tickInterval;
	private static volatile Map<Item, Integer> limits = Map.of();

	private ItemLimiterFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
		if (registered) {
			return;
		}
		registered = true;

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (!enabled) {
				return;
			}
			server.execute(() -> enforceOnPlayer(handler.player));
		});

		ServerTickEvents.END_SERVER_TICK.register(new ServerTickEvents.EndTick() {
			private int ticks;

			@Override
			public void onEndTick(MinecraftServer server) {
				int interval = tickInterval;
				if (!enabled || interval <= 0) {
					return;
				}
				Map<Item, Integer> current = limits;
				if (current.isEmpty()) {
					return;
				}

				ticks++;
				if (ticks < interval) {
					return;
				}
				ticks = 0;

				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					enforceOnPlayer(player);
				}
			}
		});
	}

	public static void reload(SmpCoreConfig config) {
		enabled = config.itemLimiter.enabled;
		preventPickup = config.itemLimiter.preventPickup;
		dropExcess = config.itemLimiter.dropExcess;
		actionBar = config.messages.actionBar;
		minMillisBetweenNotices = config.messages.minMillisBetweenNotices;
		tickInterval = config.itemLimiter.scanSeconds <= 0 ? -1 : Math.max(1, config.itemLimiter.scanSeconds * 20);
		limits = buildLimits(config);
	}

	public static boolean preventPickup() {
		return enabled && preventPickup && !limits.isEmpty();
	}

	public static boolean wouldExceed(ServerPlayer player, ItemStack incoming) {
		if (!enabled || incoming == null || incoming.isEmpty()) {
			return false;
		}
		Integer max = limits.get(incoming.getItem());
		if (max == null || max <= 0) {
			return false;
		}
		return countItem(player, incoming.getItem()) >= max;
	}

	public static void notifyLimit(ServerPlayer player, Item item) {
		if (!NOTICE_COOLDOWNS.shouldNotify(player.getUUID(), minMillisBetweenNotices)) {
			return;
		}
		int max = Objects.requireNonNullElse(limits.get(item), 0);
		String name = item.getDescriptionId();
		// Keep message simple and readable.
		String msg = "Item limit reached (" + max + ").";
		if (actionBar) {
			TextUtil.actionBar(player, msg);
		} else {
			TextUtil.chat(player, msg);
		}
	}

	public static void enforceOnPlayer(ServerPlayer player) {
		if (!enabled) {
			return;
		}
		Map<Item, Integer> current = limits;
		if (current.isEmpty()) {
			return;
		}

		Map<Item, Integer> counts = new HashMap<>();
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack == null || stack.isEmpty()) {
				continue;
			}
			Item item = stack.getItem();
			Integer max = current.get(item);
			if (max == null || max <= 0) {
				continue;
			}

			int before = counts.getOrDefault(item, 0);
			int allowedLeft = max - before;
			if (allowedLeft <= 0) {
				removeAll(player, i, stack);
				continue;
			}

			int keep = Math.min(stack.getCount(), allowedLeft);
			if (keep < stack.getCount()) {
				ItemStack excess = stack.copy();
				excess.setCount(stack.getCount() - keep);
				stack.setCount(keep);
				player.getInventory().setItem(i, stack);
				dropOrDelete(player, excess);
			}
			counts.put(item, before + keep);
		}

		player.containerMenu.broadcastChanges();
	}

	private static void removeAll(ServerPlayer player, int slot, ItemStack stack) {
		player.getInventory().setItem(slot, ItemStack.EMPTY);
		dropOrDelete(player, stack);
	}

	private static void dropOrDelete(ServerPlayer player, ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return;
		}
		if (dropExcess) {
			player.drop(stack, false);
		}
	}

	private static int countItem(ServerPlayer player, Item item) {
		int count = 0;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack == null || stack.isEmpty()) {
				continue;
			}
			if (stack.getItem() == item) {
				count += stack.getCount();
			}
		}
		return count;
	}

	private static Map<Item, Integer> buildLimits(SmpCoreConfig config) {
		Map<Item, Integer> map = new HashMap<>();
		for (SmpCoreConfig.ItemLimit raw : config.itemLimiter.limits) {
			if (raw == null || raw.id == null || raw.id.isBlank()) {
				continue;
			}
			Identifier id = IdUtil.parse(raw.id.trim());
			if (id == null) {
				continue;
			}
			Item item = BuiltInRegistries.ITEM.getValue(id);
			if (item == null || item == Items.AIR) {
				continue;
			}
			int max = Math.max(0, raw.max);
			if (max <= 0) {
				continue;
			}
			map.put(item, max);
		}
		return Map.copyOf(map);
	}
}

