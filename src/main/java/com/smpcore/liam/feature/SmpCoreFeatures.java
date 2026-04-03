package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;

public final class SmpCoreFeatures {
	private SmpCoreFeatures() {
	}

	public static void initAll(SmpCoreConfig config) {
		AntiXrayFeature.init(config);
		ProximityChatFeature.init(config);
		DimensionRulesFeature.init(config);
		OnePlayerSleepFeature.init(config);
		DeathSoundFeature.init(config);
		BedAndAnchorBanFeature.init(config);
		EnderChestLockFeature.init(config);
		ItemBanFeature.init(config);
		PotionBanFeature.init(config);
		PvpFeature.init(config);
		SpectatorAfterDeathFeature.init(config);
		CombatFeature.init(config);
		ShieldTweaksFeature.init(config);
		BreachSwapBanFeature.init(config);
		CooldownFeature.init(config);
		MaceCooldownFeature.init(config);
		WardenHeartFeature.init(config);
		EffectBanFeature.init(config);
		EnchantmentRulesFeature.init(config);
		FirstJoinKitFeature.init(config);
		ItemLimiterFeature.init(config);
		VillagerRestockFeature.init(config);
		PvpRulesFeature.init(config);
		CraftingRulesFeature.init(config);
	}

	public static void reloadAll(SmpCoreConfig config) {
		AntiXrayFeature.reload(config);
		ProximityChatFeature.reload(config);
		DimensionRulesFeature.reload(config);
		OnePlayerSleepFeature.reload(config);
		DeathSoundFeature.reload(config);
		BedAndAnchorBanFeature.reload(config);
		EnderChestLockFeature.reload(config);
		ItemBanFeature.reload(config);
		PotionBanFeature.reload(config);
		PvpFeature.reload(config);
		SpectatorAfterDeathFeature.reload(config);
		CombatFeature.reload(config);
		ShieldTweaksFeature.reload(config);
		BreachSwapBanFeature.reload(config);
		CooldownFeature.reload(config);
		MaceCooldownFeature.reload(config);
		WardenHeartFeature.reload(config);
		EffectBanFeature.reload(config);
		EnchantmentRulesFeature.reload(config);
		FirstJoinKitFeature.reload(config);
		ItemLimiterFeature.reload(config);
		VillagerRestockFeature.reload(config);
		PvpRulesFeature.reload(config);
		CraftingRulesFeature.reload(config);
	}
}
