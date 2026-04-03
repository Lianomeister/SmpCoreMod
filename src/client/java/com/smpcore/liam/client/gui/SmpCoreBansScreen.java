package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreCategoryList;
import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public final class SmpCoreBansScreen extends SmpCoreMenuBase {
	private SmpCoreCategoryList list;

	public SmpCoreBansScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Bans"), parent, config);
	}

	@Override
	protected void init() {
		int w = Math.min(420, this.width - 40);
		int left = (this.width - w) / 2;
		int top = 44;
		int listBottom = this.height - 44;

		list = addRenderableWidget(new SmpCoreCategoryList(this.minecraft, w, this.height, top, listBottom, 44));
		list.setLeftPos(left);

		addBan(new ItemStack(Items.RED_BED), "Bed bombing", "Prevents bed explosions outside the Overworld.", () -> config.bans.banBedBombing, v -> config.bans.banBedBombing = v);
		addBan(new ItemStack(Items.RESPAWN_ANCHOR), "Anchor bombing", "Prevents respawn anchor explosions outside the Nether.", () -> config.bans.banAnchorBombing, v -> config.bans.banAnchorBombing = v);
		addBan(new ItemStack(Items.TNT_MINECART), "TNT minecarts", "Disables TNT minecart usage.", () -> config.bans.banTntMinecarts, v -> config.bans.banTntMinecarts = v);
		addBan(new ItemStack(Items.MACE), "Mace", "Disables using the mace item.", () -> config.bans.banMace, v -> config.bans.banMace = v);
		addBan(new ItemStack(Items.ENDER_PEARL), "Ender pearls", "Disables ender pearl usage.", () -> config.bans.banPearls, v -> config.bans.banPearls = v);
		addBan(new ItemStack(Items.END_CRYSTAL), "End crystals", "Disables end crystal placement/usage.", () -> config.bans.banCrystals, v -> config.bans.banCrystals = v);
		addBan(new ItemStack(Items.TIPPED_ARROW), "Tipped arrows", "Disables tipped arrows.", () -> config.bans.banTippedArrows, v -> config.bans.banTippedArrows = v);

		addBan(new ItemStack(Items.DROPPER), "Remove banned items on join", "Removes banned items from players when they join.", () -> config.bans.removeBannedItemsOnJoin, v -> config.bans.removeBannedItemsOnJoin = v);

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.ENDER_CHEST),
				Component.literal("No Ender Chest items"),
				Component.literal("Items that cannot be stored in Ender Chests (comma-separated IDs)."),
				List.of(
						Component.literal("Comma-separated item ids, e.g.: minecraft:elytra, minecraft:netherite_ingot"),
						Component.literal("These items can still be kept in your inventory / normal chests.")
				),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal("No Ender Chest items"),
						Component.literal("Comma-separated item ids"),
						String.join(", ", config.storage.noEnderChestItems),
						List.of(Component.literal("Example: minecraft:elytra, minecraft:netherite_ingot")),
						txt -> {
							String[] parts = txt.split(",");
							ArrayList<String> ids = new ArrayList<>();
							for (String p : parts) {
								String id = p.trim();
								if (!id.isEmpty()) {
									ids.add(id);
								}
							}
							config.storage.noEnderChestItems = ids;
						})),
				() -> Component.literal(config.storage.noEnderChestItems.isEmpty() ? "None" : (config.storage.noEnderChestItems.size() + " items"))
		));

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.BARRIER),
				Component.literal("Custom banned items"),
				Component.literal("Comma-separated IDs of items to ban."),
				List.of(
						Component.literal("Example: minecraft:ender_pearl, minecraft:elytra"),
						Component.literal("Use commas to separate ids.")
				),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal("Custom banned items"),
						Component.literal("Comma-separated item ids"),
						String.join(", ", config.bans.bannedItems),
						List.of(
								Component.literal("Example: minecraft:ender_pearl, minecraft:elytra"),
								Component.literal("Use commas to separate ids.")
						),
						txt -> config.bans.bannedItems = parseIdList(txt)
				)),
				() -> Component.literal(config.bans.bannedItems.isEmpty() ? "None" : (config.bans.bannedItems.size() + " items"))
		));

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderSmpBackground(graphics);
		renderSmpHeader(graphics, getTitle(), Component.literal("Toggle banned mechanics and items"));
		super.render(graphics, mouseX, mouseY, partialTick);
		if (list != null) {
			List<Component> tooltip = list.consumeHoveredTooltip();
			if (tooltip != null) {
				graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
			}
		}
	}

	private void addBan(ItemStack icon, String title, String desc, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				icon,
				Component.literal(title),
				Component.literal(desc),
				List.of(Component.literal(desc)),
				() -> {
					boolean next = !getter.getAsBoolean();
					setter.accept(next);
					saveToServer();
				},
				() -> Component.literal(getter.getAsBoolean() ? "Banned" : "Allowed")
		));
	}

	private static List<String> parseIdList(String raw) {
		List<String> out = new ArrayList<>();
		for (String part : raw.split(",")) {
			String trimmed = part.trim();
			if (!trimmed.isEmpty()) {
				out.add(trimmed);
			}
		}
		return out;
	}
}
