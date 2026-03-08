package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.NoticeCooldowns;
import com.smpcore.liam.util.TextUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BreachSwapBanFeature {
	private static final NoticeCooldowns NOTICE_COOLDOWNS = new NoticeCooldowns();
	private static boolean registered;

	private static final Map<UUID, Long> lockUntilMillisByPlayer = new ConcurrentHashMap<>();

	private static volatile boolean enabled;
	private static volatile int lockMs;
	private static volatile boolean actionBar;
	private static volatile long minMillisBetweenNotices;

	private BreachSwapBanFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
		if (registered) {
			return;
		}
		registered = true;

		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, damageSource, baseDamage, finalDamage, blocked) -> {
			if (!enabled || lockMs <= 0) {
				return;
			}
			if (!(damageSource.getEntity() instanceof ServerPlayer attacker)) {
				return;
			}

			ItemStack weapon = attacker.getMainHandItem();
			if (weapon.isEmpty()) {
				return;
			}

			Registry<Enchantment> reg = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			Enchantment breach = reg.getValueOrThrow(Enchantments.BREACH);
			int lvl = EnchantmentHelper.getItemEnchantmentLevel(reg.wrapAsHolder(breach), weapon);
			if (lvl <= 0) {
				return;
			}

			lockUntilMillisByPlayer.put(attacker.getUUID(), System.currentTimeMillis() + lockMs);
		});
	}

	public static void reload(SmpCoreConfig config) {
		enabled = config.combat.enabled && config.combat.banBreachSwapping;
		lockMs = Math.max(0, config.combat.breachSwapLockMs);
		actionBar = config.messages.actionBar;
		minMillisBetweenNotices = config.messages.minMillisBetweenNotices;
	}

	public static boolean shouldBlockSwap(ServerPlayer player) {
		if (!enabled || lockMs <= 0) {
			return false;
		}
		Long until = lockUntilMillisByPlayer.get(player.getUUID());
		if (until == null) {
			return false;
		}
		long now = System.currentTimeMillis();
		if (now > until) {
			lockUntilMillisByPlayer.remove(player.getUUID(), until);
			return false;
		}
		return true;
	}

	public static void notifyBlocked(ServerPlayer player) {
		if (!NOTICE_COOLDOWNS.shouldNotify(player.getUUID(), minMillisBetweenNotices)) {
			return;
		}
		if (actionBar) {
			TextUtil.actionBar(player, "No breach swapping.");
		} else {
			TextUtil.chat(player, "No breach swapping.");
		}
	}
}

