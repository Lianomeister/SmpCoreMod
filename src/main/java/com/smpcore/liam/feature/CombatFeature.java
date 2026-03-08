package com.smpcore.liam.feature;

import com.smpcore.liam.combat.CombatState;
import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.NoticeCooldowns;
import com.smpcore.liam.util.TextUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

public final class CombatFeature {
	private static boolean registered;
	private static final NoticeCooldowns NOTICE_COOLDOWNS = new NoticeCooldowns();

	private static volatile boolean actionBar;
	private static volatile long minMillisBetweenNotices;

	private CombatFeature() {
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
			if (!(damageSource.getEntity() instanceof ServerPlayer attacker)) {
				return;
			}
			CombatState.tag(victim.getUUID());
			CombatState.tag(attacker.getUUID());
		});

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!(player instanceof ServerPlayer serverPlayer)) {
				return InteractionResult.PASS;
			}
			if (!CombatState.antiRestock() || !CombatState.isInCombat(serverPlayer.getUUID())) {
				return InteractionResult.PASS;
			}

			var state = world.getBlockState(hitResult.getBlockPos());
			boolean blockedContainer =
					state.is(Blocks.CHEST)
							|| state.is(Blocks.TRAPPED_CHEST)
							|| state.is(Blocks.ENDER_CHEST)
							|| state.is(Blocks.BARREL)
							|| state.is(BlockTags.SHULKER_BOXES);

			if (!blockedContainer) {
				return InteractionResult.PASS;
			}

			notify(serverPlayer, "No restocking while in combat.");
			return InteractionResult.FAIL;
		});
	}

	public static void reload(SmpCoreConfig config) {
		CombatState.reload(config);
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

