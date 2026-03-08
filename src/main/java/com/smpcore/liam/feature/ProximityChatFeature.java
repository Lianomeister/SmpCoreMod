package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public final class ProximityChatFeature {
	private static volatile boolean enabled;
	private static volatile double radius;
	private static volatile boolean affectsCommands;
	private static volatile boolean includeSpectators;
	private static volatile boolean opsBypass;

	private ProximityChatFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
	}

	public static void reload(SmpCoreConfig config) {
		enabled = config.messages.proximityChatEnabled;
		radius = Math.max(0.0, config.messages.proximityChatRadius);
		affectsCommands = config.messages.proximityChatAffectsCommands;
		includeSpectators = config.messages.proximityChatIncludeSpectators;
		opsBypass = config.messages.proximityChatOpsBypass;
	}

	public static boolean enabled() {
		return enabled && radius > 0.0;
	}

	public static boolean affectsCommands() {
		return affectsCommands;
	}

	public static boolean isInRange(ServerPlayer sender, ServerPlayer target) {
		if (target == sender) {
			return true;
		}
		if (opsBypass && target.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS))) {
			return true;
		}
		if (!includeSpectators && target.isSpectator()) {
			return false;
		}
		if (!target.level().dimension().equals(sender.level().dimension())) {
			return false;
		}
		double r = radius;
		return target.distanceToSqr(sender) <= (r * r);
	}
}
