package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;

public final class VillagerRestockFeature {
	private static volatile boolean infiniteRestock;

	private VillagerRestockFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
	}

	public static void reload(SmpCoreConfig config) {
		infiniteRestock = config.villagers.infiniteRestock;
	}

	public static boolean infiniteRestock() {
		return infiniteRestock;
	}
}

