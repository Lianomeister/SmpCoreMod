package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.client.gui.widget.SmpCoreCategoryList;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class SmpCoreAntiXrayScreen extends SmpCoreMenuBase {
	private SmpCoreCategoryList list;

	public SmpCoreAntiXrayScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Anti X-Ray"), parent, config);
	}

	@Override
	protected void init() {
		int w = Math.min(420, this.width - 40);
		int left = (this.width - w) / 2;
		int top = 44;
		int listBottom = this.height - 44;

		list = addRenderableWidget(new SmpCoreCategoryList(this.minecraft, w, this.height, top, listBottom, 44));
		list.setLeftPos(left);

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.DIAMOND_ORE),
				Component.literal("Enabled"),
				Component.literal("Master toggle for Anti X-Ray engine."),
				List.of(Component.literal("Master toggle for Anti X-Ray engine.")),
				() -> {
					config.gameplay.antiXrayEnabled = !config.gameplay.antiXrayEnabled;
					saveToServer();
				},
				() -> Component.literal(config.gameplay.antiXrayEnabled ? "Yes" : "No")
		));

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.REDSTONE),
				Component.literal("Engine mode"),
				Component.literal("Switch between engine modes (implementation WIP)."),
				List.of(Component.literal("Switch between engine modes (implementation WIP).")),
				() -> {
					config.gameplay.antiXrayMode = next(config.gameplay.antiXrayMode);
					saveToServer();
				},
				() -> Component.literal(config.gameplay.antiXrayMode.name())
		));

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.literal("Choose how the server hides ore information"), width / 2, 30, 0xB9B9B9);
		super.render(graphics, mouseX, mouseY, partialTick);
		if (list != null) {
			List<Component> tooltip = list.consumeHoveredTooltip();
			if (tooltip != null) {
				graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
			}
		}
	}

	private static SmpCoreConfig.AntiXrayMode next(SmpCoreConfig.AntiXrayMode mode) {
		SmpCoreConfig.AntiXrayMode[] values = SmpCoreConfig.AntiXrayMode.values();
		return values[(mode.ordinal() + 1) % values.length];
	}
}

