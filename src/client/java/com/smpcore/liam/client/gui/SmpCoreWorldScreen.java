package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.client.gui.widget.SmpCoreCategoryList;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class SmpCoreWorldScreen extends SmpCoreMenuBase {
	private SmpCoreCategoryList list;

	public SmpCoreWorldScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("World"), parent, config);
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
				Component.literal("Anti X-Ray"),
				Component.literal("Configure Anti X-Ray engine mode."),
				List.of(Component.literal("Configure Anti X-Ray engine mode.")),
				() -> this.minecraft.setScreen(new SmpCoreAntiXrayScreen(this, config)),
				() -> Component.literal(config.gameplay.antiXrayEnabled ? config.gameplay.antiXrayMode.name() : "Disabled")
		));

		addToggle(new ItemStack(Items.RED_BED), "One player sleep", "Skip the night when one player sleeps (Overworld).", () -> config.sleep.onePlayerSleep, v -> config.sleep.onePlayerSleep = v);
		addToggle(new ItemStack(Items.SUNFLOWER), "Clear weather on skip", "If enabled, one player sleep also clears rain/thunder.", () -> config.sleep.clearWeatherOnSkip, v -> config.sleep.clearWeatherOnSkip = v);

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.PLAYER_HEAD),
				Component.literal("Sleep min players"),
				Component.literal("Minimum players that must be sleeping to skip the night."),
				List.of(Component.literal("Minimum players required."), Component.literal("Clamped to at least 1.")),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal("Sleep min players"),
						Component.literal("Minimum sleeping players"),
						Integer.toString(config.sleep.minPlayers),
						List.of(Component.literal("Example: 1"), Component.literal("Clamped to at least 1.")),
						txt -> {
							try {
								config.sleep.minPlayers = Math.max(1, Integer.parseInt(txt.trim()));
							} catch (Exception ignored) {
							}
						})),
				() -> Component.literal(Integer.toString(config.sleep.minPlayers))
		));

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.CLOCK),
				Component.literal("Sleep required percent"),
				Component.literal("Percent of players that must be sleeping to skip the night (0 = disabled)."),
				List.of(Component.literal("0 disables percent requirement."), Component.literal("Range: 0-100")),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal("Sleep required percent"),
						Component.literal("Percent requirement (0-100)"),
						Integer.toString(config.sleep.requiredPercent),
						List.of(Component.literal("0 = only min players"), Component.literal("Example: 50 = half the players")),
						txt -> {
							try {
								int v = Integer.parseInt(txt.trim());
								config.sleep.requiredPercent = Math.max(0, Math.min(100, v));
							} catch (Exception ignored) {
							}
						})),
				() -> Component.literal(config.sleep.requiredPercent + "%")
		));

		addToggle(new ItemStack(Items.OBSIDIAN), "Nether enabled", "Allow entering the Nether via portals.", () -> config.dimensions.allowNether, v -> config.dimensions.allowNether = v);
		addToggle(new ItemStack(Items.ENDER_EYE), "End enabled", "Allow entering The End via end portals.", () -> config.dimensions.allowEnd, v -> config.dimensions.allowEnd = v);

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderSmpBackground(graphics);
		renderSmpHeader(graphics, getTitle(), Component.literal("World rules and Anti X-Ray"));
		super.render(graphics, mouseX, mouseY, partialTick);
		if (list != null) {
			List<Component> tooltip = list.consumeHoveredTooltip();
			if (tooltip != null) {
				graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
			}
		}
	}

	private void addToggle(ItemStack icon, String title, String desc, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
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
				() -> Component.literal(getter.getAsBoolean() ? "Enabled" : "Disabled")
		));
	}
}

