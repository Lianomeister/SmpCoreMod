package com.smpcore.liam.feature;

import com.smpcore.liam.combat.CombatState;
import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.NoticeCooldowns;
import com.smpcore.liam.util.TextUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
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
	private static volatile boolean announceCombatLog;
	private static volatile boolean showCombatTagCountdown;
	private static volatile boolean notifyOnCombatTag;
	private static volatile int tagSeconds;

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

			boolean victimNew = CombatState.tagIfEnabled(victim.getUUID());
			boolean attackerNew = CombatState.tagIfEnabled(attacker.getUUID());

			if (notifyOnCombatTag) {
				if (victimNew) {
					TextUtil.actionBar(victim, "Combat tagged (" + tagSeconds + "s).");
				}
				if (attackerNew) {
					TextUtil.actionBar(attacker, "Combat tagged (" + tagSeconds + "s).");
				}
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			if (!announceCombatLog) {
				return;
			}
			ServerPlayer player = handler.player;
			long remaining = CombatState.remainingMillis(player.getUUID());
			if (remaining <= 0) {
				return;
			}
			long seconds = (remaining + 999) / 1000;
			server.getPlayerList().broadcastSystemMessage(
					Component.literal(player.getScoreboardName() + " combat-logged (" + seconds + "s remaining)."),
					false
			);
		});

		ServerTickEvents.END_SERVER_TICK.register(new ServerTickEvents.EndTick() {
			private int ticks;

			@Override
			public void onEndTick(MinecraftServer server) {
				if (!showCombatTagCountdown) {
					return;
				}
				ticks++;
				if (ticks < 20) {
					return;
				}
				ticks = 0;

				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					long remaining = CombatState.remainingMillis(player.getUUID());
					if (remaining <= 0) {
						continue;
					}
					long seconds = (remaining + 999) / 1000;
					TextUtil.actionBar(player, "In combat: " + seconds + "s");
				}
			}
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
		announceCombatLog = config.combat.announceCombatLog;
		showCombatTagCountdown = config.combat.showCombatTagCountdown;
		notifyOnCombatTag = config.combat.notifyOnCombatTag;
		tagSeconds = Math.max(0, config.combat.tagSeconds);
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

