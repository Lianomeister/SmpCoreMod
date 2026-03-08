package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.NoticeCooldowns;
import com.smpcore.liam.util.TextUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MaceCooldownFeature {
	private static boolean registered;
	private static final NoticeCooldowns NOTICE_COOLDOWNS = new NoticeCooldowns();

	private static final Map<UUID, Long> nextUseMillisByPlayer = new ConcurrentHashMap<>();

	private static volatile boolean actionBar;
	private static volatile long minMillisBetweenNotices;
	private static volatile long cooldownMs;

	private MaceCooldownFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
		if (registered) {
			return;
		}
		registered = true;

		AttackEntityCallback.EVENT.register((attacker, world, hand, target, hitResult) -> {
			if (!(attacker instanceof ServerPlayer player)) {
				return InteractionResult.PASS;
			}
			if (cooldownMs <= 0) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getMainHandItem();
			if (!stack.is(Items.MACE)) {
				return InteractionResult.PASS;
			}

			long now = System.currentTimeMillis();
			Long next = nextUseMillisByPlayer.get(player.getUUID());
			if (next != null && now < next) {
				notify(player, "Your mace is on cooldown.");
				return InteractionResult.FAIL;
			}
			return InteractionResult.PASS;
		});

		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, damageSource, baseDamage, finalDamage, blocked) -> {
			if (cooldownMs <= 0) {
				return;
			}
			if (blocked || finalDamage <= 0F) {
				return;
			}
			if (!(damageSource.getEntity() instanceof ServerPlayer attacker)) {
				return;
			}
			if (!(entity instanceof ServerPlayer)) {
				return;
			}

			ItemStack stack = attacker.getMainHandItem();
			if (!stack.is(Items.MACE)) {
				return;
			}

			nextUseMillisByPlayer.put(attacker.getUUID(), System.currentTimeMillis() + cooldownMs);
		});
	}

	public static void reload(SmpCoreConfig config) {
		actionBar = config.messages.actionBar;
		minMillisBetweenNotices = config.messages.minMillisBetweenNotices;
		cooldownMs = Math.max(0, (long) config.cooldowns.maceSeconds * 1000L);
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

