package com.smpcore.liam.feature;

import com.smpcore.liam.SmpCore;
import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.IdUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class FirstJoinKitFeature {
	private static final String TAG = "smpcore_first_join_kit";
	private static boolean registered;

	private static volatile boolean enabled;
	private static volatile boolean onlyOnce;
	private static volatile List<KitEntry> kit = List.of();

	private FirstJoinKitFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
		if (registered) {
			return;
		}
		registered = true;

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			server.execute(() -> tryGrant(handler.player));
		});
	}

	public static void reload(SmpCoreConfig config) {
		enabled = config.kits.firstJoin.enabled;
		onlyOnce = config.kits.firstJoin.onlyOnce;
		kit = parseKit(config.kits.firstJoin.items);
	}

	private static void tryGrant(ServerPlayer player) {
		if (!enabled) {
			return;
		}
		if (kit.isEmpty()) {
			return;
		}
		if (onlyOnce && player.getTags().contains(TAG)) {
			return;
		}

		boolean gaveAnything = false;
		for (KitEntry entry : kit) {
			Item item = BuiltInRegistries.ITEM.getValue(entry.id);
			if (item == null || item == net.minecraft.world.item.Items.AIR) {
				continue;
			}
			int remaining = entry.count;
			while (remaining > 0) {
				ItemStack stack = new ItemStack(item);
				int give = Math.min(remaining, stack.getMaxStackSize());
				stack.setCount(give);
				boolean added = player.getInventory().add(stack);
				if (!added || !stack.isEmpty()) {
					player.drop(stack, false);
				}
				remaining -= give;
				gaveAnything = true;
			}
		}

		if (gaveAnything && onlyOnce) {
			player.addTag(TAG);
		}
		if (gaveAnything) {
			SmpCore.LOGGER.info("Granted first-join kit to {}", player.getScoreboardName());
		}
	}

	private static List<KitEntry> parseKit(List<SmpCoreConfig.KitItem> items) {
		List<KitEntry> out = new ArrayList<>();
		for (SmpCoreConfig.KitItem item : items) {
			if (item == null || item.id == null || item.id.isBlank()) {
				continue;
			}
			Identifier id = IdUtil.parse(item.id.trim());
			if (id == null) {
				continue;
			}
			int count = Math.max(1, item.count);
			out.add(new KitEntry(id, count));
		}
		return List.copyOf(out);
	}

	private record KitEntry(Identifier id, int count) {
	}
}
