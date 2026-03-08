package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.NoticeCooldowns;
import com.smpcore.liam.util.TextUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ShieldTweaksFeature {
	private static final NoticeCooldowns NOTICE_COOLDOWNS = new NoticeCooldowns();
	private static boolean registered;

	private static volatile int shieldCooldownTicks;
	private static volatile boolean maceStunsShield;
	private static volatile int maceStunTicks;

	private static volatile boolean actionBar;
	private static volatile long minMillisBetweenNotices;

	private ShieldTweaksFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
		if (registered) {
			return;
		}
		registered = true;

		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, damageSource, baseDamage, finalDamage, blocked) -> {
			if (!(entity instanceof ServerPlayer victim)) {
				return;
			}
			if (!blocked) {
				return;
			}

			if (shieldCooldownTicks > 0) {
				victim.getCooldowns().addCooldown(new ItemStack(Items.SHIELD), shieldCooldownTicks);
			}

			if (maceStunsShield && maceStunTicks > 0 && damageSource.getEntity() instanceof ServerPlayer attacker) {
				ItemStack weapon = attacker.getMainHandItem();
				if (weapon.is(Items.MACE)) {
					victim.getCooldowns().addCooldown(new ItemStack(Items.SHIELD), Math.max(maceStunTicks, shieldCooldownTicks));
					notify(victim, "Shield stunned.");
				}
			}
		});
	}

	public static void reload(SmpCoreConfig config) {
		shieldCooldownTicks = Math.max(0, config.combat.shieldCooldownSeconds) * 20;
		maceStunsShield = config.combat.maceStunsShield;
		maceStunTicks = Math.max(0, config.combat.maceShieldStunSeconds) * 20;
		actionBar = config.messages.actionBar;
		minMillisBetweenNotices = config.messages.minMillisBetweenNotices;
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
