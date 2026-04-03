package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ClickVillagersFeature {
	private static boolean registered;
	private static volatile boolean enabled;

	private ClickVillagersFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
		if (registered) {
			return;
		}
		registered = true;

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!enabled) {
				return InteractionResult.PASS;
			}
			if (world.isClientSide()) {
				return InteractionResult.PASS;
			}
			if (!(player instanceof ServerPlayer serverPlayer)) {
				return InteractionResult.PASS;
			}
			if (!(entity instanceof Villager villager)) {
				return InteractionResult.PASS;
			}
			if (!serverPlayer.isCrouching()) {
				return InteractionResult.PASS;
			}
			if (!serverPlayer.getItemInHand(hand).isEmpty()) {
				return InteractionResult.PASS;
			}
			if (!villager.isAlive()) {
				return InteractionResult.PASS;
			}

			ItemStack egg = new ItemStack(Items.VILLAGER_SPAWN_EGG, 1);
			boolean added = serverPlayer.getInventory().add(egg);
			if (!added) {
				serverPlayer.drop(egg, false);
			}

			villager.discard();
			serverPlayer.containerMenu.broadcastChanges();
			return InteractionResult.SUCCESS;
		});
	}

	public static void reload(SmpCoreConfig config) {
		enabled = config.gameplay.clickVillagers;
	}
}
