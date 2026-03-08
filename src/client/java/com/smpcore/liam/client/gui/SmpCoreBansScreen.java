package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreCategoryList;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

		addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
				.bounds(left, this.height - 32, w, 20)
				.build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.literal("Toggle banned mechanics and items"), width / 2, 30, 0xB9B9B9);
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
}

