package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.NoticeCooldowns;
import com.smpcore.liam.util.TextUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PvpRulesFeature {
	private static final NoticeCooldowns NOTICE_COOLDOWNS = new NoticeCooldowns();
	private static boolean registered;

	private static final Map<UUID, AfkEntry> afkByPlayer = new ConcurrentHashMap<>();

	private static volatile boolean enabledNoNakedKilling;
	private static volatile boolean allowNakedVsNaked;
	private static volatile boolean enabledNoAfkKilling;
	private static volatile long afkMillis;
	private static volatile boolean actionBar;
	private static volatile long minMillisBetweenNotices;

	private PvpRulesFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
		if (registered) {
			return;
		}
		registered = true;

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.player;
			afkByPlayer.put(player.getUUID(), AfkEntry.fromPlayer(player, System.currentTimeMillis()));
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			afkByPlayer.remove(handler.player.getUUID());
		});

		ServerTickEvents.END_SERVER_TICK.register(new ServerTickEvents.EndTick() {
			private int ticks;

			@Override
			public void onEndTick(MinecraftServer server) {
				// Update AFK state at ~4 Hz, enough for responsiveness without extra work.
				ticks++;
				if (ticks < 5) {
					return;
				}
				ticks = 0;

				if (!enabledNoAfkKilling) {
					return;
				}

				long now = System.currentTimeMillis();
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					afkByPlayer.compute(player.getUUID(), (uuid, prev) -> {
						AfkEntry base = prev == null ? AfkEntry.fromPlayer(player, now) : prev;
						return base.updateFromPlayer(player, now);
					});
				}
			}
		});
	}

	public static void reload(SmpCoreConfig config) {
		enabledNoNakedKilling = config.combat.noNakedKilling;
		allowNakedVsNaked = config.combat.allowNakedVsNaked;
		enabledNoAfkKilling = config.combat.noAfkKilling;
		afkMillis = Math.max(0L, (long) config.combat.afkSeconds * 1000L);
		actionBar = config.messages.actionBar;
		minMillisBetweenNotices = config.messages.minMillisBetweenNotices;
	}

	public static boolean noNakedKilling() {
		return enabledNoNakedKilling;
	}

	public static boolean allowNakedVsNaked() {
		return allowNakedVsNaked;
	}

	public static boolean noAfkKilling() {
		return enabledNoAfkKilling && afkMillis > 0L;
	}

	public static boolean isNaked(ServerPlayer player) {
		return player.getArmorValue() <= 0;
	}

	public static boolean isAfk(ServerPlayer player) {
		if (!noAfkKilling()) {
			return false;
		}
		AfkEntry entry = afkByPlayer.get(player.getUUID());
		long last = entry == null ? System.currentTimeMillis() : entry.lastActiveMillis;
		return System.currentTimeMillis() - last >= afkMillis;
	}

	public static void notifyAttacker(ServerPlayer attacker, String message) {
		if (!NOTICE_COOLDOWNS.shouldNotify(attacker.getUUID(), minMillisBetweenNotices)) {
			return;
		}
		if (actionBar) {
			TextUtil.actionBar(attacker, message);
		} else {
			TextUtil.chat(attacker, message);
		}
	}

	private static final class AfkEntry {
		final double x;
		final double y;
		final double z;
		final float yaw;
		final float pitch;
		final long lastActiveMillis;

		private AfkEntry(double x, double y, double z, float yaw, float pitch, long lastActiveMillis) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.yaw = yaw;
			this.pitch = pitch;
			this.lastActiveMillis = lastActiveMillis;
		}

		static AfkEntry fromPlayer(ServerPlayer player, long now) {
			return new AfkEntry(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), now);
		}

		AfkEntry updateFromPlayer(ServerPlayer player, long now) {
			double nx = player.getX();
			double ny = player.getY();
			double nz = player.getZ();
			float nyaw = player.getYRot();
			float npitch = player.getXRot();

			boolean moved = distSqr(nx, ny, nz, x, y, z) > 0.02 * 0.02;
			boolean rotated = Math.abs(nyaw - yaw) > 1.0f || Math.abs(npitch - pitch) > 1.0f;
			if (moved || rotated) {
				return new AfkEntry(nx, ny, nz, nyaw, npitch, now);
			}
			// Keep latest position/rotation even if inactive to avoid false positives after teleports with no rotation change.
			return new AfkEntry(nx, ny, nz, nyaw, npitch, lastActiveMillis);
		}

		private static double distSqr(double ax, double ay, double az, double bx, double by, double bz) {
			double dx = ax - bx;
			double dy = ay - by;
			double dz = az - bz;
			return dx * dx + dy * dy + dz * dz;
		}
	}
}
