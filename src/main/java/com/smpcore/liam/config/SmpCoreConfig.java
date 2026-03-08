package com.smpcore.liam.config;

import java.util.ArrayList;
import java.util.List;

public final class SmpCoreConfig {
	public int configVersion = 1;

	public Messages messages = new Messages();
	public Gameplay gameplay = new Gameplay();
	public Dimensions dimensions = new Dimensions();
	public Sleep sleep = new Sleep();
	public VoiceChat voiceChat = new VoiceChat();
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

		public boolean invisibilityAnonymousKills = true;
		public boolean wardenHeartDrop = true;
	}

	public static final class Dimensions {
		public boolean allowNether = true;
		public boolean allowEnd = true;
	}

	public static final class Sleep {
		public boolean onePlayerSleep = false;
		public boolean clearWeatherOnSkip = true;

		/**
		 * Minimum required sleeping players to skip the night. Clamped to at least 1.
		 */
		public int minPlayers = 1;

		/**
		 * Percentage of (eligible) players required to be sleeping to skip the night.
		 * Set to 0 to use {@link #minPlayers} only.
		 */
		public int requiredPercent = 0;
	}

	public enum AntiXrayMode {
		BASIC,
		FAST,
		STRICT
	}

	public static final class VoiceChat {
		/**
		 * If enabled, SMP Core will write Simple Voice Chat settings to {@code config/voicechat/voicechat-server.properties}.
		 */
		public boolean manageSimpleVoiceChat = false;

		public int port = 24454;
		public String bindAddress = "";
		public String voiceHost = "";

		public double maxVoiceDistance = 48.0;
		public double whisperDistance = 24.0;
		public double broadcastRange = -1.0;

		public VoiceChatCodec codec = VoiceChatCodec.VOIP;
		public int mtuSize = 1024;
		public int keepAliveMs = 1000;

		public boolean enableGroups = true;
		public boolean allowRecording = true;
		public boolean spectatorInteraction = false;
		public boolean spectatorPlayerPossession = false;
		public boolean forceVoiceChat = false;
		public boolean allowPings = true;
		public boolean useNatives = true;
		public int loginTimeoutMs = 10_000;
	}

	public enum VoiceChatCodec {
		VOIP,
		AUDIO,
		RESTRICTED_LOWDELAY
	}

	public Combat combat = new Combat();
	public Cooldowns cooldowns = new Cooldowns();

	public static final class Combat {
		public boolean enabled = true;
		public int tagSeconds = 15;
		public boolean antiRestock = true;
		public boolean antiElytra = true;

		/**
		 * Multiplies ALL damage taken by players. Set 1.0 for vanilla behavior.
		 */
		public double playerDamageMultiplier = 1.0;

		/**
		 * PvP-only damage multiplier. Set 1.0 for vanilla behavior.
		 */
		public double pvpDamageMultiplier = 1.0;

		public int shieldCooldownSeconds = 0;
		public boolean maceStunsShield = false;
		public int maceShieldStunSeconds = 0;

		public boolean banBreachSwapping = false;
		public int breachSwapLockMs = 750;
	}

	public static final class Cooldowns {
		public int pearlSeconds = 0;
		public int eGapSeconds = 0;
		public int windChargeSeconds = 0;
		public int maceSeconds = 0;
		public int riptideSeconds = 0;
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
