package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.NoticeCooldowns;
import com.smpcore.liam.util.TextUtil;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class CooldownFeature {
	private static boolean registered;
	private static final NoticeCooldowns NOTICE_COOLDOWNS = new NoticeCooldowns();

	private static volatile boolean actionBar;
	private static volatile long minMillisBetweenNotices;
	private static volatile long pearlCooldownMs;
	private static volatile long eGapCooldownMs;
	private static volatile long windChargeCooldownMs;
	private static volatile long riptideCooldownMs;

	private CooldownFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
		if (registered) {
			return;
		}
		registered = true;

		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (!(player instanceof ServerPlayer serverPlayer)) {
				return InteractionResult.PASS;
			}

			ItemStack stack = serverPlayer.getItemInHand(hand);
			if (stack.isEmpty()) {
				return InteractionResult.PASS;
			}

			long cooldownMs = cooldownFor(serverPlayer, stack);
			if (cooldownMs <= 0) {
				return InteractionResult.PASS;
			}

			if (serverPlayer.getCooldowns().isOnCooldown(stack)) {
				notify(serverPlayer, "On cooldown.");
				return InteractionResult.FAIL;
			}

			int ticks = (int) Math.ceil(cooldownMs / 50.0);
			if (ticks > 0) {
				serverPlayer.getCooldowns().addCooldown(stack, ticks);
			}
			return InteractionResult.PASS;
		});
	}

	public static void reload(SmpCoreConfig config) {
		actionBar = config.messages.actionBar;
		minMillisBetweenNotices = config.messages.minMillisBetweenNotices;

		pearlCooldownMs = Math.max(0, (long) config.cooldowns.pearlSeconds * 1000L);
		eGapCooldownMs = Math.max(0, (long) config.cooldowns.eGapSeconds * 1000L);
		windChargeCooldownMs = Math.max(0, (long) config.cooldowns.windChargeSeconds * 1000L);
		riptideCooldownMs = Math.max(0, (long) config.cooldowns.riptideSeconds * 1000L);
	}

	private static long cooldownFor(ServerPlayer player, ItemStack stack) {
		Item item = stack.getItem();
		if (item == Items.ENDER_PEARL) {
			return pearlCooldownMs;
		}
		if (item == Items.ENCHANTED_GOLDEN_APPLE) {
			return eGapCooldownMs;
		}
		if (item == Items.WIND_CHARGE) {
			return windChargeCooldownMs;
		}
		if (item == Items.TRIDENT && riptideCooldownMs > 0) {
			Registry<Enchantment> reg = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			Enchantment riptide = reg.getValueOrThrow(Enchantments.RIPTIDE);
			int lvl = EnchantmentHelper.getItemEnchantmentLevel(reg.wrapAsHolder(riptide), stack);
			if (lvl > 0) {
				return riptideCooldownMs;
			}
		}
		return 0;
	}

	private static void notify(ServerPlayer player, String message) {
		if (!NOTICE_COOLDOWNS.shouldNotify(player.getUUID(), minMillisBetweenNotices)) {
			return;
		}
		if (actionBar) {
			TextUtil.actionBar(player, message);
		} else {
			TextUtil.chat(player, message);
		}
	}
}
