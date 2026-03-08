package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.IdUtil;
import com.smpcore.liam.util.NoticeCooldowns;
import com.smpcore.liam.util.TextUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.Set;

public final class EnderChestLockFeature {
	private static final NoticeCooldowns NOTICE_COOLDOWNS = new NoticeCooldowns();

	private static volatile boolean actionBar;
	private static volatile long minMillisBetweenNotices;
	private static volatile Set<Item> lockedItems = Set.of();

	private EnderChestLockFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
	}

	public static void reload(SmpCoreConfig config) {
		actionBar = config.messages.actionBar;
		minMillisBetweenNotices = config.messages.minMillisBetweenNotices;
		lockedItems = buildLockedItems(config);
	}

	public static boolean isEnabled() {
		return !lockedItems.isEmpty();
	}

	public static boolean isLocked(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		return lockedItems.contains(stack.getItem());
	}

	public static void notify(ServerPlayer player, String message) {
		if (!NOTICE_COOLDOWNS.shouldNotify(player.getUUID(), minMillisBetweenNotices)) {
			return;
		}
		if (actionBar) {
			TextUtil.actionBar(player, message);
		} else {
			TextUtil.chat(player, message);
		}
	}

	private static Set<Item> buildLockedItems(SmpCoreConfig config) {
		Set<Item> set = new HashSet<>();
		for (String rawId : config.storage.noEnderChestItems) {
			Identifier id = IdUtil.parse(rawId);
			if (id == null) {
				continue;
			}
			Item item = BuiltInRegistries.ITEM.getValue(id);
			if (item != null && item != Items.AIR) {
				set.add(item);
			}
		}
		return set;
	}
}

