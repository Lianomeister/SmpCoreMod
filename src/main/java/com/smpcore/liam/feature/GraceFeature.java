package com.smpcore.liam.feature;

import com.smpcore.liam.SmpCore;
import com.smpcore.liam.config.SmpCoreConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public final class GraceFeature {
	private static boolean registered;

	private static volatile long graceEndsAtEpochMillis;
	private static volatile boolean announceGrace;

	private GraceFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
		if (registered) {
			return;
		}
		registered = true;

		ServerTickEvents.END_SERVER_TICK.register(new ServerTickEvents.EndTick() {
			private int ticks;

			@Override
			public void onEndTick(MinecraftServer server) {
				long endsAt = graceEndsAtEpochMillis;
				if (endsAt <= 0L) {
					return;
				}

				ticks++;
				if (ticks < 20) {
					return;
				}
				ticks = 0;

				long now = System.currentTimeMillis();
				if (now < endsAt) {
					return;
				}

				// End grace: PvP on, clear timer.
				SmpCore.updateConfig(c -> {
					if (c.start.graceEndsAtEpochMillis <= 0L) {
						return;
					}
					c.start.graceEndsAtEpochMillis = 0L;
					c.gameplay.pvpEnabled = true;
				});

				if (announceGrace) {
					server.getPlayerList().broadcastSystemMessage(Component.literal("Grace period ended. PvP is now enabled."), false);
				}
			}
		});
	}

	public static void reload(SmpCoreConfig config) {
		graceEndsAtEpochMillis = config.start.graceEndsAtEpochMillis;
		announceGrace = config.start.announceGrace;
	}

	public static boolean graceActive() {
		long endsAt = graceEndsAtEpochMillis;
		return endsAt > 0L && System.currentTimeMillis() < endsAt;
	}

	public static long remainingSeconds() {
		long endsAt = graceEndsAtEpochMillis;
		if (endsAt <= 0L) {
			return 0L;
		}
		long rem = endsAt - System.currentTimeMillis();
		if (rem <= 0L) {
			return 0L;
		}
		return (rem + 999) / 1000;
	}
}

