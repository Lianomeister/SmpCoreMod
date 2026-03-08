package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreCategoryList;
import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class SmpCoreGameplayScreen extends SmpCoreMenuBase {
	private SmpCoreCategoryList list;

	public SmpCoreGameplayScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Gameplay"), parent, config);
	}

	@Override
	protected void init() {
		int w = Math.min(420, this.width - 40);
		int left = (this.width - w) / 2;
		int top = 44;
		int listBottom = this.height - 44;

		list = addRenderableWidget(new SmpCoreCategoryList(this.minecraft, w, this.height, top, listBottom, 44));
		list.setLeftPos(left);

		addToggle(new ItemStack(Items.IRON_SWORD), "PvP", "Enable/disable player vs player damage.", () -> config.gameplay.pvpEnabled, v -> config.gameplay.pvpEnabled = v);

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.DIAMOND_ORE),
				Component.literal("Anti X-Ray"),
				Component.literal("Configure Anti X-Ray engine mode."),
				List.of(Component.literal("Configure Anti X-Ray engine mode.")),
				() -> this.minecraft.setScreen(new SmpCoreAntiXrayScreen(this, config)),
				() -> Component.literal(config.gameplay.antiXrayEnabled ? config.gameplay.antiXrayMode.name() : "Disabled")
		));

		addToggle(new ItemStack(Items.GHAST_TEAR), "Spectator after death", "Hardcore-like: after death, respawn in spectator.", () -> config.death.spectatorAfterDeath, v -> config.death.spectatorAfterDeath = v);

		addToggle(new ItemStack(Items.RED_BED), "One player sleep", "Skip the night when one player sleeps (Overworld).", () -> config.sleep.onePlayerSleep, v -> config.sleep.onePlayerSleep = v);
		addToggle(new ItemStack(Items.SUNFLOWER), "Clear weather on skip", "If enabled, one player sleep also clears rain/thunder.", () -> config.sleep.clearWeatherOnSkip, v -> config.sleep.clearWeatherOnSkip = v);

		addToggle(new ItemStack(Items.OBSIDIAN), "Nether enabled", "Allow entering the Nether via portals.", () -> config.dimensions.allowNether, v -> config.dimensions.allowNether = v);
		addToggle(new ItemStack(Items.ENDER_EYE), "End enabled", "Allow entering The End via end portals.", () -> config.dimensions.allowEnd, v -> config.dimensions.allowEnd = v);

		addToggle(new ItemStack(Items.POTION), "Anonymous invis kills", "If an invisible player kills someone, their name is hidden in the death message.", () -> config.gameplay.invisibilityAnonymousKills, v -> config.gameplay.invisibilityAnonymousKills = v);
		addToggle(new ItemStack(Items.HEART_OF_THE_SEA), "Warden heart drop", "Killing a warden drops a Warden Heart (usable in custom recipes).", () -> config.gameplay.wardenHeartDrop, v -> config.gameplay.wardenHeartDrop = v);

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.literal("Core server rules and world settings"), width / 2, 30, 0xB9B9B9);
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
