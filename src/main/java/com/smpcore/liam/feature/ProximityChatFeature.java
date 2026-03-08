package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.server.level.ServerPlayer;

public final class ProximityChatFeature {
	private static volatile boolean enabled;
	private static volatile double radius;

	private ProximityChatFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
	}

	public static void reload(SmpCoreConfig config) {
		enabled = config.messages.proximityChatEnabled;
		radius = Math.max(0.0, config.messages.proximityChatRadius);
	}

	public static boolean enabled() {
		return enabled && radius > 0.0;
	}

	public static boolean isInRange(ServerPlayer sender, ServerPlayer target) {
		if (target == sender) {
			return true;
		}
		if (!target.level().dimension().equals(sender.level().dimension())) {
			return false;
		}
		double r = radius;
		return target.distanceToSqr(sender) <= (r * r);
	}
}

