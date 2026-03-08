package com.smpcore.liam.config;

import java.util.ArrayList;
import java.util.List;

public final class SmpCoreConfig {
	public int configVersion = 1;

	public Messages messages = new Messages();
	public Gameplay gameplay = new Gameplay();
	public Bans bans = new Bans();
	public Potions potions = new Potions();
	public Death death = new Death();
	public Effects effects = new Effects();
	public Enchantments enchantments = new Enchantments();

	public static final class Messages {
		public boolean actionBar = true;
		public long minMillisBetweenNotices = 1500;
	}

	public static final class Gameplay {
		public boolean pvpEnabled = true;
		public boolean antiXrayEnabled = false;
		public AntiXrayMode antiXrayMode = AntiXrayMode.BASIC;
	}

	public enum AntiXrayMode {
		BASIC,
		FAST,
		STRICT
	}

	public Combat combat = new Combat();
	public Cooldowns cooldowns = new Cooldowns();

	public static final class Combat {
		public boolean enabled = true;
		public int tagSeconds = 15;
		public boolean antiRestock = true;
		public boolean antiElytra = true;

		/**
		 * PvP-only damage multiplier. Set 1.0 for vanilla behavior.
		 */
		public double pvpDamageMultiplier = 1.0;
	}

	public static final class Cooldowns {
		public int pearlSeconds = 0;
		public int gapSeconds = 0;
		public int windChargeSeconds = 0;
	}

	public static final class Bans {
		public boolean banBedBombing = true;
		public boolean banAnchorBombing = true;
		public boolean banMace = true;
		public boolean banTntMinecarts = true;
		public boolean banPearls = false;
		public boolean banCrystals = false;
		public boolean banTippedArrows = false;

		public boolean removeBannedItemsOnJoin = true;

		/**
		 * Item IDs like "minecraft:ender_pearl". Use this for server-specific bans.
		 */
		public List<String> bannedItems = new ArrayList<>();
	}

	public static final class Potions {
		public boolean banAll = false;

		/**
		 * Status effect IDs like "minecraft:strength". If empty, only {@link #banAll} is enforced.
		 */
		public List<String> bannedPotionEffects = new ArrayList<>();
	}

	public static final class Death {
		public boolean spectatorAfterDeath = false;
	}

	public static final class Effects {
		/**
		 * Status effect IDs like "minecraft:strength".
		 */
		public List<String> bannedEffects = new ArrayList<>();
		public int scanSeconds = 2;
	}

	public static final class Enchantments {
		/**
		 * Enchantment IDs like "minecraft:breach".
		 */
		public List<String> bannedEnchantments = new ArrayList<>();

		public Limits limits = new Limits();
		public boolean clampOnJoin = true;
		public int scanSeconds = 30;
	}

	public static final class Limits {
		/**
		 * Set to 0 to disable clamping for that enchantment.
		 */
		public int sharpnessMax = 5;
		public int protectionMax = 4;
	}
}
