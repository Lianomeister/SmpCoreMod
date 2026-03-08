package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.IdUtil;
import com.smpcore.liam.util.NoticeCooldowns;
import com.smpcore.liam.util.TextUtil;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.Set;

public final class ItemBanFeature {
	private static final NoticeCooldowns NOTICE_COOLDOWNS = new NoticeCooldowns();
	private static boolean registered;

	private static volatile boolean removeBannedItemsOnJoin;
	private static volatile boolean actionBar;
	private static volatile long minMillisBetweenNotices;
	private static volatile Set<Item> bannedItems = Set.of();

	private ItemBanFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);

		if (registered) {
			return;
		}
		registered = true;

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (!removeBannedItemsOnJoin || bannedItems.isEmpty()) {
				return;
			}
			removeBannedItems(handler.player, bannedItems);
		});

		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (!(player instanceof ServerPlayer serverPlayer)) {
				return InteractionResult.PASS;
			}

			Set<Item> banned = bannedItems;
			if (banned.isEmpty()) {
				return InteractionResult.PASS;
			}

			ItemStack stack = serverPlayer.getItemInHand(hand);
			if (!stack.isEmpty() && banned.contains(stack.getItem())) {
				notify(serverPlayer, "That item is disabled.");
				return InteractionResult.FAIL;
			}

			return InteractionResult.PASS;
		});

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!(player instanceof ServerPlayer serverPlayer)) {
				return InteractionResult.PASS;
			}

			Set<Item> banned = bannedItems;
			if (banned.isEmpty()) {
				return InteractionResult.PASS;
			}

			ItemStack stack = serverPlayer.getItemInHand(hand);
			if (!stack.isEmpty() && banned.contains(stack.getItem())) {
				notify(serverPlayer, "You can't use that item to attack.");
				return InteractionResult.FAIL;
			}
			return InteractionResult.PASS;
		});
	}

	public static void reload(SmpCoreConfig config) {
		removeBannedItemsOnJoin = config.bans.removeBannedItemsOnJoin;
		actionBar = config.messages.actionBar;
		minMillisBetweenNotices = config.messages.minMillisBetweenNotices;
		bannedItems = buildBannedItems(config);
	}

	private static Set<Item> buildBannedItems(SmpCoreConfig config) {
		Set<Item> banned = new HashSet<>();

		if (config.bans.banTntMinecarts) {
			banned.add(Items.TNT_MINECART);
		}
		if (config.bans.banMace) {
			banned.add(Items.MACE);
		}
		if (config.bans.banPearls) {
			banned.add(Items.ENDER_PEARL);
		}
		if (config.bans.banCrystals) {
			banned.add(Items.END_CRYSTAL);
		}
		if (config.bans.banTippedArrows) {
			banned.add(Items.TIPPED_ARROW);
		}

		for (String rawId : config.bans.bannedItems) {
			Identifier id = IdUtil.parse(rawId);
			if (id == null) {
				continue;
			}
			Item item = BuiltInRegistries.ITEM.getValue(id);
			if (item != null && item != Items.AIR) {
				banned.add(item);
			}
		}

		return banned;
	}

	private static void removeBannedItems(ServerPlayer player, Set<Item> banned) {
		boolean removedAny = false;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (!stack.isEmpty() && banned.contains(stack.getItem())) {
				player.getInventory().setItem(i, ItemStack.EMPTY);
				removedAny = true;
			}
		}

		if (removedAny) {
			player.containerMenu.broadcastChanges();
		}
	}

	private static void notify(ServerPlayer player, String message) {
		if (!NOTICE_COOLDOWNS.shouldNotify(player.getUUID(), minMillisBetweenNotices)) {
			return;
		}

		if (actionBar) {
			TextUtil.actionBar(player, message);
		} else {
			TextUtil.chat(player, message);
		}
	}
}
