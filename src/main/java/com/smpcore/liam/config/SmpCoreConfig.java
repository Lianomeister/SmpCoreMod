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
	public Storage storage = new Storage();
	public Bans bans = new Bans();
	public Potions potions = new Potions();
	public Death death = new Death();
	public Effects effects = new Effects();
	public Enchantments enchantments = new Enchantments();
	public ItemLimiter itemLimiter = new ItemLimiter();
	public Villagers villagers = new Villagers();

	public static final class Messages {
		public boolean actionBar = true;
		public long minMillisBetweenNotices = 1500;

		/**
		 * If enabled, player chat is only sent to players within {@link #proximityChatRadius} blocks.
		 */
		public boolean proximityChatEnabled = false;

		/**
		 * Radius in blocks for proximity chat.
		 */
		public double proximityChatRadius = 64.0;

		/**
		 * If enabled, proximity chat also applies to server broadcast chat coming from commands
		 * (e.g. /say, /me). If disabled, those remain global.
		 */
		public boolean proximityChatAffectsCommands = true;

		/**
		 * If enabled, spectators can receive proximity chat.
		 */
		public boolean proximityChatIncludeSpectators = false;

		/**
		 * If enabled, ops always receive proximity chat regardless of range.
		 */
		public boolean proximityChatOpsBypass = false;
	}

	public static final class Gameplay {
		public boolean pvpEnabled = true;
		public boolean antiXrayEnabled = false;
		public AntiXrayMode antiXrayMode = AntiXrayMode.BASIC;
		public boolean antiXrayExposeCheck = true;
		public boolean antiXrayHideSpawners = true;
		public boolean antiXrayUseCustomHiddenBlocks = false;
		public List<String> antiXrayCustomHiddenBlocks = new ArrayList<>();

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

	public static final class Storage {
		/**
		 * Item IDs like "minecraft:elytra" which cannot be put into Ender Chests.
		 * This is useful for "vault" rules where certain items must stay in normal inventories/chests.
		 */
		public List<String> noEnderChestItems = new ArrayList<>();
	}

	public Combat combat = new Combat();
	public Cooldowns cooldowns = new Cooldowns();
	public Kits kits = new Kits();

	public static final class Combat {
		public boolean enabled = true;
		public int tagSeconds = 15;

		/**
		 * Broadcasts a server message when a player disconnects while combat-tagged.
		 */
		public boolean announceCombatLog = false;

		/**
		 * Shows an actionbar countdown while a player is combat-tagged.
		 */
		public boolean showCombatTagCountdown = false;

		/**
		 * Sends an actionbar message when a player is (newly) combat-tagged.
		 */
		public boolean notifyOnCombatTag = false;

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

		/**
		 * Caps mace damage dealt to players (after multipliers). Set 0 to disable.
		 * Value is in raw damage (2.0 = 1 heart).
		 */
		public double maceDamageCap = 0.0;

		/**
		 * If enabled, players cannot be reduced below {@link #immortalityMinHealth} unless holding a totem.
		 */
		public boolean immortalityEnabled = false;

		/**
		 * Minimum health to keep players at when immortality is enabled (1.0 = half a heart).
		 */
		public double immortalityMinHealth = 1.0;

		/**
		 * If enabled, void damage can still kill players even with immortality.
		 */
		public boolean immortalityAllowVoidDeath = true;

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

	public static final class Kits {
		public FirstJoinKit firstJoin = new FirstJoinKit();
	}

	public static final class FirstJoinKit {
		public boolean enabled = false;

		/**
		 * If true, the kit is only granted once per player, tracked via a persistent player tag.
		 */
		public boolean onlyOnce = true;

		public List<KitItem> items = new ArrayList<>();
	}

	public static final class KitItem {
		/**
		 * Item ID like "minecraft:bread".
		 */
		public String id = "minecraft:bread";

		public int count = 1;
	}

	public static final class Bans {
		public boolean banBedBombing = false;
		public boolean banAnchorBombing = false;
		public boolean banMace = false;
		public boolean banTntMinecarts = false;
		public boolean banPearls = false;
		public boolean banCrystals = false;
		public boolean banTippedArrows = false;

		public boolean removeBannedItemsOnJoin = false;

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

		public boolean customDeathSoundEnabled = false;
		public String customDeathSoundId = "minecraft:entity.player.death";
		public double customDeathSoundVolume = 1.0;
		public double customDeathSoundPitch = 1.0;
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

	public static final class ItemLimiter {
		public boolean enabled = false;

		/**
		 * How often to scan and enforce (seconds). Set <= 0 to disable periodic scanning (join/pickup still apply).
		 */
		public int scanSeconds = 10;

		/**
		 * If enabled, picking up a limited item is blocked once the player reached the limit.
		 */
		public boolean preventPickup = true;

		/**
		 * If enabled, excess items are dropped at the player instead of deleted.
		 */
		public boolean dropExcess = true;

		public List<ItemLimit> limits = new ArrayList<>();
	}

	public static final class ItemLimit {
		/**
		 * Item ID like "minecraft:ender_pearl".
		 */
		public String id = "minecraft:ender_pearl";

		/**
		 * Maximum total count across player inventory.
		 */
		public int max = 16;
	}

	public static final class Villagers {
		/**
		 * If enabled, villager trades never run out of stock.
		 */
		public boolean infiniteRestock = false;
	}
}
