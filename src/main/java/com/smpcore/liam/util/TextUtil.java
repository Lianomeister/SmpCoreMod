package com.smpcore.liam.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class TextUtil {
	private TextUtil() {
	}

	public static void actionBar(ServerPlayer player, String message) {
		player.displayClientMessage(Component.literal(message), true);
	}

	public static void chat(ServerPlayer player, String message) {
		player.displayClientMessage(Component.literal(message), false);
	}
}
