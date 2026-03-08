package com.smpcore.liam.gui;

import com.smpcore.liam.SmpCore;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

public final class SmpCoreAdminMenu extends ChestMenu {
	private static final int ROWS = 6;
	private static final int SIZE = ROWS * 9;

	private final SimpleContainer container;
	private AdminMenuPage page;

	public SmpCoreAdminMenu(int containerId, Inventory playerInventory, AdminMenuPage page) {
		super(MenuType.GENERIC_9x6, containerId, playerInventory, new SimpleContainer(SIZE), ROWS);
		this.container = (SimpleContainer) getContainer();
		this.page = page;
		refresh();
	}

	@Override
	public void clicked(int slotId, int button, ClickType clickType, Player player) {
		if (slotId < 0 || slotId >= SIZE) {
			return;
		}

		boolean left = clickType == ClickType.PICKUP && button == 0;
		boolean right = clickType == ClickType.PICKUP && button == 1;
		boolean drop = clickType == ClickType.THROW;
		if (!left && !right && !drop) {
			setCarried(ItemStack.EMPTY);
			return;
		}

		if (page == AdminMenuPage.MAIN) {
			handleMain(player, slotId, left, right, drop);
		} else if (page == AdminMenuPage.POTIONS) {
			handlePotions(player, slotId, left, right, drop);
		}

		setCarried(ItemStack.EMPTY);
		refresh();
		broadcastChanges();
	}

	private void handleMain(Player player, int slotId, boolean left, boolean right, boolean drop) {
		switch (slotId) {
			case 10 -> toggle("PvP", c -> c.gameplay.pvpEnabled = !c.gameplay.pvpEnabled, left || right || drop);
			case 11 -> toggle("Anti X-Ray", c -> c.gameplay.antiXrayEnabled = !c.gameplay.antiXrayEnabled, left || right || drop);
			case 12 -> toggle("Spectator after death", c -> c.death.spectatorAfterDeath = !c.death.spectatorAfterDeath, left || right || drop);
			case 13 -> toggle("Bed bombing", c -> c.bans.banBedBombing = !c.bans.banBedBombing, left || right || drop);
			case 14 -> toggle("Anchor bombing", c -> c.bans.banAnchorBombing = !c.bans.banAnchorBombing, left || right || drop);
			case 15 -> toggle("TNT minecarts", c -> c.bans.banTntMinecarts = !c.bans.banTntMinecarts, left || right || drop);
			case 16 -> toggle("Mace", c -> c.bans.banMace = !c.bans.banMace, left || right || drop);
			case 19 -> toggle("Ender pearls", c -> c.bans.banPearls = !c.bans.banPearls, left || right || drop);
			case 20 -> toggle("End crystals", c -> c.bans.banCrystals = !c.bans.banCrystals, left || right || drop);
			case 21 -> {
				if (left || right || drop) {
					page = AdminMenuPage.POTIONS;
				}
			}
			case 49 -> {
				if (left || right || drop) {
					if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
						serverPlayer.closeContainer();
					}
				}
			}
			default -> {
			}
		}
	}

	private void handlePotions(Player player, int slotId, boolean left, boolean right, boolean drop) {
		switch (slotId) {
			case 45 -> {
				if (left || right || drop) {
					page = AdminMenuPage.MAIN;
				}
			}
			case 10 -> toggle("Ban all potions", c -> c.potions.banAll = !c.potions.banAll, left || right || drop);
			case 12 -> toggleEffect("minecraft:strength", left || right || drop);
			case 13 -> toggleEffect("minecraft:speed", left || right || drop);
			case 14 -> toggleEffect("minecraft:invisibility", left || right || drop);
			case 15 -> toggleEffect("minecraft:regeneration", left || right || drop);
			case 16 -> toggleEffect("minecraft:poison", left || right || drop);
			default -> {
			}
		}
	}

	private void toggleEffect(String effectId, boolean doIt) {
		if (!doIt) {
			return;
		}
		SmpCore.updateConfig(cfg -> {
			if (cfg.potions.bannedPotionEffects.contains(effectId)) {
				cfg.potions.bannedPotionEffects.remove(effectId);
			} else {
				cfg.potions.bannedPotionEffects.add(effectId);
			}
		});
	}

	private void toggle(String label, java.util.function.Consumer<SmpCoreConfig> mutator, boolean doIt) {
		if (!doIt) {
			return;
		}
		SmpCore.updateConfig(mutator);
	}

	private void refresh() {
		for (int i = 0; i < SIZE; i++) {
			container.setItem(i, ItemStack.EMPTY);
		}

		if (page == AdminMenuPage.MAIN) {
			renderMain();
		} else if (page == AdminMenuPage.POTIONS) {
			renderPotions();
		}
	}

	private void renderMain() {
		SmpCoreConfig cfg = SmpCore.getConfig();

		container.setItem(10, icon(Items.IRON_SWORD, "PvP", enabled(cfg.gameplay.pvpEnabled)));
		container.setItem(11, icon(Items.DIAMOND_ORE, "Anti X-Ray", enabled(cfg.gameplay.antiXrayEnabled), line("Placeholder (not implemented yet).", ChatFormatting.DARK_GRAY)));
		container.setItem(12, icon(Items.SKELETON_SKULL, "Spectator after death", enabled(cfg.death.spectatorAfterDeath)));

		container.setItem(13, icon(Items.RED_BED, "Bed bombing", banned(cfg.bans.banBedBombing)));
		container.setItem(14, icon(Items.RESPAWN_ANCHOR, "Anchor bombing", banned(cfg.bans.banAnchorBombing)));
		container.setItem(15, icon(Items.TNT_MINECART, "TNT minecarts", banned(cfg.bans.banTntMinecarts)));
		container.setItem(16, icon(Items.MACE, "Mace", banned(cfg.bans.banMace)));

		container.setItem(19, icon(Items.ENDER_PEARL, "Ender pearls", banned(cfg.bans.banPearls)));
		container.setItem(20, icon(Items.END_CRYSTAL, "End crystals", banned(cfg.bans.banCrystals)));
		container.setItem(21, icon(Items.POTION, "Potions...", line("Click to open potion bans.", ChatFormatting.GRAY)));

		container.setItem(49, icon(Items.BARRIER, "Close", line("Click to close.", ChatFormatting.GRAY)));
	}

	private void renderPotions() {
		SmpCoreConfig cfg = SmpCore.getConfig();

		container.setItem(45, icon(Items.ARROW, "Back", line("Return to main menu.", ChatFormatting.GRAY)));

		container.setItem(10, icon(Items.POTION, "All potions", banned(cfg.potions.banAll)));

		container.setItem(12, effectIcon(Items.BLAZE_POWDER, "Strength", "minecraft:strength", cfg));
		container.setItem(13, effectIcon(Items.SUGAR, "Speed", "minecraft:speed", cfg));
		container.setItem(14, effectIcon(Items.FERMENTED_SPIDER_EYE, "Invisibility", "minecraft:invisibility", cfg));
		container.setItem(15, effectIcon(Items.GHAST_TEAR, "Regeneration", "minecraft:regeneration", cfg));
		container.setItem(16, effectIcon(Items.SPIDER_EYE, "Poison", "minecraft:poison", cfg));
	}

	private ItemStack effectIcon(Item item, String label, String effectId, SmpCoreConfig cfg) {
		boolean banned = cfg.potions.bannedPotionEffects.contains(effectId);
		return icon(item, label + " potions", banned(banned), line(effectId, ChatFormatting.DARK_GRAY));
	}

	private static Component enabled(boolean enabled) {
		return enabled ? Component.literal("Enabled").withStyle(ChatFormatting.GREEN) : Component.literal("Disabled").withStyle(ChatFormatting.RED);
	}

	private static Component banned(boolean banned) {
		return banned ? Component.literal("Banned").withStyle(ChatFormatting.RED) : Component.literal("Allowed").withStyle(ChatFormatting.GREEN);
	}

	private static Component line(String text, ChatFormatting color) {
		return Component.literal(text).withStyle(color);
	}

	private static ItemStack icon(Item item, String title, Component... loreLines) {
		ItemStack stack = new ItemStack(item);
		stack.set(DataComponents.CUSTOM_NAME, Component.literal(title).withStyle(ChatFormatting.GOLD));

		List<Component> lines = new ArrayList<>();
		for (Component c : loreLines) {
			lines.add(c);
		}
		lines.add(Component.literal("LMB/RMB/Q to toggle").withStyle(ChatFormatting.DARK_GRAY));
		stack.set(DataComponents.LORE, new ItemLore(lines));
		return stack;
	}
}
