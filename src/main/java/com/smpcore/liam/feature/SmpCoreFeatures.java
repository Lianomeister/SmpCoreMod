package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;

public final class SmpCoreFeatures {
	private SmpCoreFeatures() {
	}

	public static void initAll(SmpCoreConfig config) {
		BedAndAnchorBanFeature.init(config);
		ItemBanFeature.init(config);
		EffectBanFeature.init(config);
		EnchantmentRulesFeature.init(config);
	}

	public static void reloadAll(SmpCoreConfig config) {
		BedAndAnchorBanFeature.reload(config);
		ItemBanFeature.reload(config);
		EffectBanFeature.reload(config);
		EnchantmentRulesFeature.reload(config);
	}
}
