package com.smpcore.liam.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public final class AdminMenu {
	private AdminMenu() {
	}

	public static void openMain(ServerPlayer player) {
		open(player, AdminMenuPage.MAIN);
	}

	public static void open(ServerPlayer player, AdminMenuPage page) {
		player.openMenu(new SimpleMenuProvider(
				(id, inv, p) -> new SmpCoreAdminMenu(id, inv, page),
				Component.literal("Settings")
		));
	}
}

