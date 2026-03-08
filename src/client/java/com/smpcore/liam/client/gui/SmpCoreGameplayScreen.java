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

		addToggle(new ItemStack(Items.OAK_SIGN), "Proximity chat", "Only players near you can see your messages.", () -> config.messages.proximityChatEnabled, v -> config.messages.proximityChatEnabled = v);
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.COMPASS),
				Component.literal("Proximity chat radius"),
				Component.literal("Radius in blocks for proximity chat."),
				List.of(Component.literal("Example: 64"), Component.literal("Set <= 0 to effectively disable radius.")),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal("Proximity chat radius"),
						Component.literal("Radius in blocks"),
						Double.toString(config.messages.proximityChatRadius),
						List.of(Component.literal("Example: 64")),
						txt -> {
							try {
								config.messages.proximityChatRadius = Math.max(0.0, Double.parseDouble(txt.trim()));
							} catch (Exception ignored) {
							}
						})),
				() -> Component.literal(trimDouble(config.messages.proximityChatRadius) + " blocks")
		));
		addToggle(new ItemStack(Items.COMMAND_BLOCK), "Proximity affects commands", "Apply proximity chat to /say and /me broadcasts.", () -> config.messages.proximityChatAffectsCommands, v -> config.messages.proximityChatAffectsCommands = v);
		addToggle(new ItemStack(Items.SPYGLASS), "Proximity include spectators", "If enabled, spectators can receive proximity chat.", () -> config.messages.proximityChatIncludeSpectators, v -> config.messages.proximityChatIncludeSpectators = v);
		addToggle(new ItemStack(Items.DIAMOND), "Proximity ops bypass", "If enabled, ops always receive proximity chat.", () -> config.messages.proximityChatOpsBypass, v -> config.messages.proximityChatOpsBypass = v);

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

	private static String trimDouble(double v) {
		String s = Double.toString(v);
		return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
	}
}
