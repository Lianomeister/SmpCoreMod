package com.smpcore.liam.config;

import java.util.ArrayList;
import java.util.List;

public final class SmpCoreConfig {
	public int configVersion = 1;

	public Messages messages = new Messages();
	public Bans bans = new Bans();
	public Effects effects = new Effects();
	public Enchantments enchantments = new Enchantments();

	public static final class Messages {
		public boolean actionBar = true;
		public long minMillisBetweenNotices = 1500;
	}

	public static final class Bans {
		public boolean banBedBombing = true;
		public boolean banAnchorBombing = true;
		public boolean banMace = true;
		public boolean banTntMinecarts = true;

		public boolean removeBannedItemsOnJoin = true;

		/**
		 * Item IDs like "minecraft:ender_pearl". Use this for server-specific bans.
		 */
		public List<String> bannedItems = new ArrayList<>();
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
