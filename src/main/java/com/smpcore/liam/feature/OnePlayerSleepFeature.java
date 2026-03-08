package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.NoticeCooldowns;
import com.smpcore.liam.util.TextUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class OnePlayerSleepFeature {
	private static final NoticeCooldowns NOTICE_COOLDOWNS = new NoticeCooldowns();
	private static boolean registered;

	private static volatile boolean enabled;
	private static volatile boolean clearWeatherOnSkip;
	private static volatile int minPlayers;
	private static volatile int requiredPercent;
	private static volatile boolean actionBar;
	private static volatile long minMillisBetweenNotices;

	private OnePlayerSleepFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);

		if (registered) {
			return;
		}
		registered = true;

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (!enabled) {
				return;
			}

			for (ServerLevel level : server.getAllLevels()) {
				if (!level.dimension().equals(Level.OVERWORLD)) {
					continue;
				}

				int eligible = 0;
				int sleepingReady = 0;
				for (ServerPlayer player : level.players()) {
					if (player.isSpectator()) {
						continue;
					}
					eligible++;
					if (player.isSleeping() && player.isSleepingLongEnough()) {
						sleepingReady++;
					}
				}

				if (sleepingReady <= 0) {
					continue;
				}

				int required = computeRequired(eligible);
				if (sleepingReady < required) {
					continue;
				}

				long dayTime = level.getDayTime();
				long nextMorning = dayTime + 24000L - (dayTime % 24000L);
				level.setDayTime(nextMorning);

				if (clearWeatherOnSkip) {
					level.setWeatherParameters(0, 0, false, false);
					level.resetWeatherCycle();
				}

				for (ServerPlayer player : level.players()) {
					if (player.isSleeping()) {
						player.stopSleepInBed(false, true);
					}
				}
				level.updateSleepingPlayerList();

				notifyAll(level, "Night skipped (" + sleepingReady + "/" + required + " sleeping).");
			}
		});
	}

	public static void reload(SmpCoreConfig config) {
		enabled = config.sleep.onePlayerSleep;
		clearWeatherOnSkip = config.sleep.clearWeatherOnSkip;
		minPlayers = Math.max(1, config.sleep.minPlayers);
		requiredPercent = Math.max(0, Math.min(100, config.sleep.requiredPercent));
		actionBar = config.messages.actionBar;
		minMillisBetweenNotices = config.messages.minMillisBetweenNotices;
	}

	private static int computeRequired(int eligiblePlayers) {
		int required = minPlayers;
		if (requiredPercent > 0 && eligiblePlayers > 0) {
			int byPercent = (int) Math.ceil((eligiblePlayers * (double) requiredPercent) / 100.0);
			required = Math.max(required, byPercent);
		}
		return Math.max(1, required);
	}

	private static void notifyAll(ServerLevel level, String message) {
		for (ServerPlayer player : level.players()) {
			if (!NOTICE_COOLDOWNS.shouldNotify(player.getUUID(), minMillisBetweenNotices)) {
				continue;
			}
			if (actionBar) {
				TextUtil.actionBar(player, message);
			} else {
				TextUtil.chat(player, message);
			}
		}
	}
}
