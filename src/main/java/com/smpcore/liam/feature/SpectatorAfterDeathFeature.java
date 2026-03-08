package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.world.level.GameType;

public final class SpectatorAfterDeathFeature {
	private static boolean registered;
	private static volatile boolean spectatorAfterDeath;

	private SpectatorAfterDeathFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
		if (registered) {
			return;
		}
		registered = true;

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			if (alive) {
				return;
			}
			if (!spectatorAfterDeath) {
				return;
			}
			newPlayer.setGameMode(GameType.SPECTATOR);
		});
	}

	public static void reload(SmpCoreConfig config) {
		spectatorAfterDeath = config.death.spectatorAfterDeath;
	}
}

