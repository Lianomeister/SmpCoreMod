package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class PvpFeature {
	private static boolean registered;
	private static volatile boolean pvpEnabled = true;

	private PvpFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
		if (registered) {
			return;
		}
		registered = true;

		AttackEntityCallback.EVENT.register((attacker, world, hand, target, hitResult) -> {
			if (pvpEnabled) {
				return InteractionResult.PASS;
			}
			if (!(attacker instanceof ServerPlayer)) {
				return InteractionResult.PASS;
			}
			if (isPlayer(target)) {
				return InteractionResult.FAIL;
			}
			return InteractionResult.PASS;
		});
	}

	public static void reload(SmpCoreConfig config) {
		pvpEnabled = config.gameplay.pvpEnabled;
	}

	private static boolean isPlayer(Entity entity) {
		return entity instanceof Player;
	}
}

