package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;

public final class DimensionRulesFeature {
	private static volatile boolean allowNether;
	private static volatile boolean allowEnd;

	private DimensionRulesFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
	}

	public static void reload(SmpCoreConfig config) {
		allowNether = config.dimensions.allowNether;
		allowEnd = config.dimensions.allowEnd;
	}

	public static boolean allowNether() {
		return allowNether;
	}

	public static boolean allowEnd() {
		return allowEnd;
	}
}

