package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;

public final class CraftingRulesFeature {
	private static volatile boolean oneCraftRecipes;

	private CraftingRulesFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
	}

	public static void reload(SmpCoreConfig config) {
		oneCraftRecipes = config.gameplay.oneCraftRecipes;
	}

	public static boolean oneCraftRecipes() {
		return oneCraftRecipes;
	}
}

