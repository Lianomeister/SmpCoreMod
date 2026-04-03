package com.smpcore.liam.combat;

import com.smpcore.liam.config.SmpCoreConfig;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatState {
	private static final Map<UUID, Long> combatUntilMillisByPlayer = new ConcurrentHashMap<>();

	private static volatile boolean enabled = true;
	private static volatile long tagMillis = 15_000L;
	private static volatile boolean antiRestock = true;
	private static volatile boolean antiElytra = true;
	private static volatile double playerDamageMultiplier = 1.0;
	private static volatile double pvpDamageMultiplier = 1.0;
	private static volatile double maceDamageCap = 0.0;
	private static volatile boolean immortalityEnabled = false;
	private static volatile double immortalityMinHealth = 1.0;
	private static volatile boolean immortalityAllowVoidDeath = true;

	private CombatState() {
	}

	public static void reload(SmpCoreConfig config) {
		enabled = config.combat.enabled;
		tagMillis = Math.max(0, (long) config.combat.tagSeconds * 1000L);
		antiRestock = config.combat.antiRestock;
		antiElytra = config.combat.antiElytra;
		playerDamageMultiplier = config.combat.playerDamageMultiplier;
		pvpDamageMultiplier = config.combat.pvpDamageMultiplier;
		maceDamageCap = Math.max(0.0, config.combat.maceDamageCap);
		immortalityEnabled = config.combat.immortalityEnabled;
		immortalityMinHealth = Math.max(0.0, config.combat.immortalityMinHealth);
		immortalityAllowVoidDeath = config.combat.immortalityAllowVoidDeath;
	}

	public static void tag(UUID playerId) {
		if (!enabled || tagMillis <= 0) {
			return;
		}
		combatUntilMillisByPlayer.put(playerId, System.currentTimeMillis() + tagMillis);
	}

	/**
	 * Tags a player and returns whether they were not in combat before.
	 */
	public static boolean tagIfEnabled(UUID playerId) {
		if (!enabled || tagMillis <= 0) {
			return false;
		}
		long now = System.currentTimeMillis();
		Long until = combatUntilMillisByPlayer.get(playerId);
		boolean wasInCombat = until != null && now <= until;
		combatUntilMillisByPlayer.put(playerId, now + tagMillis);
		return !wasInCombat;
	}

	public static boolean isInCombat(UUID playerId) {
		if (!enabled) {
			return false;
		}
		Long until = combatUntilMillisByPlayer.get(playerId);
		if (until == null) {
			return false;
		}
		long now = System.currentTimeMillis();
		if (now > until) {
			combatUntilMillisByPlayer.remove(playerId, until);
			return false;
		}
		return true;
	}

	/**
	 * Returns remaining combat-tag time in milliseconds. Returns 0 when not in combat or disabled.
	 */
	public static long remainingMillis(UUID playerId) {
		if (!enabled) {
			return 0L;
		}
		Long until = combatUntilMillisByPlayer.get(playerId);
		if (until == null) {
			return 0L;
		}
		long now = System.currentTimeMillis();
		if (now > until) {
			combatUntilMillisByPlayer.remove(playerId, until);
			return 0L;
		}
		return until - now;
	}

	public static boolean antiRestock() {
		return enabled && antiRestock;
	}

	public static boolean antiElytra() {
		return enabled && antiElytra;
	}

	public static double pvpDamageMultiplier() {
		return enabled ? pvpDamageMultiplier : 1.0;
	}

	public static double playerDamageMultiplier() {
		return enabled ? playerDamageMultiplier : 1.0;
	}

	public static double maceDamageCap() {
		return enabled ? maceDamageCap : 0.0;
	}

	public static boolean immortalityEnabled() {
		return enabled && immortalityEnabled;
	}

	public static double immortalityMinHealth() {
		return enabled ? immortalityMinHealth : 0.0;
	}

	public static boolean immortalityAllowVoidDeath() {
		return enabled && immortalityAllowVoidDeath;
	}
}
