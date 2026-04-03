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
		super(Component.literal("Misc"), parent, config);
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
		addToggle(new ItemStack(Items.CRAFTING_TABLE), "One craft recipes", "Blocks shift-click crafting from the result slot (prevents craft-all).", () -> config.gameplay.oneCraftRecipes, v -> config.gameplay.oneCraftRecipes = v);

		addToggle(new ItemStack(Items.GHAST_TEAR), "Spectator after death", "Hardcore-like: after death, respawn in spectator.", () -> config.death.spectatorAfterDeath, v -> config.death.spectatorAfterDeath = v);

		addToggle(new ItemStack(Items.NOTE_BLOCK), "Custom death sound", "Plays a custom sound when a player dies.", () -> config.death.customDeathSoundEnabled, v -> config.death.customDeathSoundEnabled = v);
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.JUKEBOX),
				Component.literal("Death sound id"),
				Component.literal("Sound event id to play on death."),
				List.of(Component.literal("Example: minecraft:entity.wither.spawn")),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal("Death sound id"),
						Component.literal("Sound event id"),
						config.death.customDeathSoundId,
						List.of(Component.literal("Example: minecraft:entity.player.death")),
						txt -> config.death.customDeathSoundId = txt.trim())),
				() -> Component.literal(config.death.customDeathSoundId)
		));
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.AMETHYST_SHARD),
				Component.literal("Death sound volume"),
				Component.literal("Volume for the custom death sound."),
				List.of(Component.literal("Example: 1.0")),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal("Death sound volume"),
						Component.literal("Volume"),
						Double.toString(config.death.customDeathSoundVolume),
						List.of(Component.literal("Example: 1.0")),
						txt -> {
							try {
								config.death.customDeathSoundVolume = Math.max(0.0, Double.parseDouble(txt.trim()));
							} catch (Exception ignored) {
							}
						})),
				() -> Component.literal(trimDouble(config.death.customDeathSoundVolume))
		));
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.ECHO_SHARD),
				Component.literal("Death sound pitch"),
				Component.literal("Pitch for the custom death sound."),
				List.of(Component.literal("Example: 1.0")),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal("Death sound pitch"),
						Component.literal("Pitch"),
						Double.toString(config.death.customDeathSoundPitch),
						List.of(Component.literal("Example: 1.0")),
						txt -> {
							try {
								config.death.customDeathSoundPitch = Math.max(0.0, Double.parseDouble(txt.trim()));
							} catch (Exception ignored) {
							}
						})),
				() -> Component.literal(trimDouble(config.death.customDeathSoundPitch))
		));

		addToggle(new ItemStack(Items.POTION), "Anonymous invis kills", "If an invisible player kills someone, their name is hidden in the death message.", () -> config.gameplay.invisibilityAnonymousKills, v -> config.gameplay.invisibilityAnonymousKills = v);
		addToggle(new ItemStack(Items.HEART_OF_THE_SEA), "Warden heart drop", "Killing a warden drops a Warden Heart (usable in custom recipes).", () -> config.gameplay.wardenHeartDrop, v -> config.gameplay.wardenHeartDrop = v);

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderSmpBackground(graphics);
		renderSmpHeader(graphics, getTitle(), Component.literal("Small extra gameplay toggles"));
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

	private static String trimDouble(double v) {
		String s = Double.toString(v);
		return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
	}
}
