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

				boolean anySleeping = false;
				boolean anyLongEnough = false;
				for (ServerPlayer player : level.players()) {
					if (player.isSleeping()) {
						anySleeping = true;
						if (player.isSleepingLongEnough()) {
							anyLongEnough = true;
							break;
						}
					}
				}

				if (!anySleeping || !anyLongEnough) {
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

				notifyAll(level, "Night skipped (one player sleep).");
			}
		});
	}

	public static void reload(SmpCoreConfig config) {
		enabled = config.sleep.onePlayerSleep;
		clearWeatherOnSkip = config.sleep.clearWeatherOnSkip;
		actionBar = config.messages.actionBar;
		minMillisBetweenNotices = config.messages.minMillisBetweenNotices;
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

