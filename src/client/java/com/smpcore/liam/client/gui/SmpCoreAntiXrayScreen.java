package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.client.gui.widget.SmpCoreCategoryList;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
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
				Component.literal("Switch between engine modes."),
				List.of(Component.literal("Switch between engine modes.")),
				() -> {
					config.gameplay.antiXrayMode = next(config.gameplay.antiXrayMode);
					saveToServer();
				},
				() -> Component.literal(config.gameplay.antiXrayMode.name())
		));

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.AIR),
				Component.literal("Expose check"),
				Component.literal("If enabled, visible ores (touching air/water) won't be hidden (BASIC/FAST)."),
				List.of(Component.literal("If disabled, ores can be hidden even when exposed.")),
				() -> {
					config.gameplay.antiXrayExposeCheck = !config.gameplay.antiXrayExposeCheck;
					saveToServer();
				},
				() -> Component.literal(config.gameplay.antiXrayExposeCheck ? "Enabled" : "Disabled")
		));

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.SPAWNER),
				Component.literal("Hide spawners"),
				Component.literal("If enabled, STRICT mode also hides dungeon spawners."),
				List.of(Component.literal("Only relevant for STRICT/custom lists.")),
				() -> {
					config.gameplay.antiXrayHideSpawners = !config.gameplay.antiXrayHideSpawners;
					saveToServer();
				},
				() -> Component.literal(config.gameplay.antiXrayHideSpawners ? "Enabled" : "Disabled")
		));

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.WRITABLE_BOOK),
				Component.literal("Custom hidden blocks"),
				Component.literal("Use a custom block list instead of preset BASIC/FAST/STRICT lists."),
				List.of(Component.literal("Comma-separated block ids, e.g.: minecraft:diamond_ore, minecraft:ancient_debris")),
				() -> {
					config.gameplay.antiXrayUseCustomHiddenBlocks = !config.gameplay.antiXrayUseCustomHiddenBlocks;
					saveToServer();
				},
				() -> Component.literal(config.gameplay.antiXrayUseCustomHiddenBlocks ? "Enabled" : "Disabled")
		));

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.DIAMOND_ORE),
				Component.literal("Hidden blocks list"),
				Component.literal("Edit the custom hidden blocks list (comma-separated)."),
				List.of(Component.literal("Only used when 'Custom hidden blocks' is enabled.")),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal("Hidden blocks list"),
						Component.literal("Comma-separated block ids"),
						String.join(", ", config.gameplay.antiXrayCustomHiddenBlocks),
						List.of(Component.literal("Example: minecraft:diamond_ore, minecraft:ancient_debris")),
						txt -> {
							String[] parts = txt.split(",");
							ArrayList<String> ids = new ArrayList<>();
							for (String p : parts) {
								String id = p.trim();
								if (!id.isEmpty()) {
									ids.add(id);
								}
							}
							config.gameplay.antiXrayCustomHiddenBlocks = ids;
						})),
				() -> Component.literal(config.gameplay.antiXrayCustomHiddenBlocks.isEmpty() ? "Empty" : (config.gameplay.antiXrayCustomHiddenBlocks.size() + " blocks"))
		));

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderSmpBackground(graphics);
		renderSmpHeader(graphics, getTitle(), Component.literal("Choose how the server hides ore information"));
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
