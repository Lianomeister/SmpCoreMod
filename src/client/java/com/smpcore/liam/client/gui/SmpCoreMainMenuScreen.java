package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreStyledButton;
import com.smpcore.liam.client.gui.widget.SmpCoreTileGrid;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class SmpCoreMainMenuScreen extends SmpCoreMenuBase {
	private SmpCoreTileGrid grid;
	private SmpCoreStyledButton prevPage;
	private SmpCoreStyledButton nextPage;

	public SmpCoreMainMenuScreen(SmpCoreConfig config) {
		super(Component.literal("SMP Core"), null, config);
	}

	@Override
	protected void init() {
		// Give the grid more room so the tiles can be larger/squarer.
		int w = Math.min(760, this.width - 40);
		int h = this.height - 92;
		int left = (this.width - w) / 2;
		int top = 44;

		this.grid = addRenderableWidget(new SmpCoreTileGrid(left, top, w, h, List.of(
				new SmpCoreTileGrid.Entry(new ItemStack(Items.GRASS_BLOCK), Component.literal("World"), Component.literal("Anti X-Ray, dimensions and sleep rules."),
						List.of(Component.literal("Anti X-Ray engine, Nether/End, one-player sleep.")),
						() -> this.minecraft.setScreen(new SmpCoreWorldScreen(this, config)),
						null),
				new SmpCoreTileGrid.Entry(new ItemStack(Items.PAPER), Component.literal("Chat"), Component.literal("Chat settings and SMP Core notices."),
						List.of(Component.literal("Actionbar notices, notice cooldown, proximity chat.")),
						() -> this.minecraft.setScreen(new SmpCoreChatScreen(this, config)),
						null),
				new SmpCoreTileGrid.Entry(new ItemStack(Items.IRON_SWORD), Component.literal("Misc"), Component.literal("Small extra gameplay toggles."),
						List.of(Component.literal("PvP toggle, death sound, warden heart drop.")),
						() -> this.minecraft.setScreen(new SmpCoreGameplayScreen(this, config)),
						null),
				new SmpCoreTileGrid.Entry(new ItemStack(Items.BARRIER), Component.literal("Bans"), Component.literal("Bed/anchor/TNT minecart/mace/pearls/crystals/tipped arrows and more."),
						List.of(Component.literal("Bed/anchor/TNT minecart/mace/pearls/crystals/tipped arrows.")),
						() -> this.minecraft.setScreen(new SmpCoreBansScreen(this, config)),
						null),
				new SmpCoreTileGrid.Entry(new ItemStack(Items.POTION), Component.literal("Potions"), Component.literal("Ban all potions or only certain effects (with a sub-menu)."),
						List.of(Component.literal("Ban all potions or specific potion effects.")),
						() -> this.minecraft.setScreen(new SmpCorePotionsScreen(this, config)),
						null),
				new SmpCoreTileGrid.Entry(new ItemStack(Items.GLOWSTONE_DUST), Component.literal("Effect bans"), Component.literal("Ban lingering status effects such as health indicators."),
						List.of(Component.literal("Block health-boost, absorption, glowing, etc.")),
						() -> this.minecraft.setScreen(new SmpCoreEffectsScreen(this, config)),
						null),
				new SmpCoreTileGrid.Entry(new ItemStack(Items.ENCHANTED_BOOK), Component.literal("Enchant Limits"), Component.literal("Clamp max levels and ban specific enchantments (server-side)."),
						List.of(Component.literal("Sharpness/protection clamping + banned enchant list (server-side).")),
						() -> this.minecraft.setScreen(new SmpCoreEnchantmentsScreen(this, config)),
						null),
				new SmpCoreTileGrid.Entry(new ItemStack(Items.SHIELD), Component.literal("Combat"), Component.literal("Combat tag, anti-restock, anti-elytra, and damage scaling."),
						List.of(Component.literal("Combat tag, anti-restock, anti-elytra, PvP damage scaling.")),
						() -> this.minecraft.setScreen(new SmpCoreCombatScreen(this, config)),
						null),
				new SmpCoreTileGrid.Entry(new ItemStack(Items.ENDER_PEARL), Component.literal("Cooldowns"), Component.literal("Pearl, E-gap, wind charge, mace and more."),
						List.of(Component.literal("Pearl/gap/wind-charge cooldowns.")),
						() -> this.minecraft.setScreen(new SmpCoreCooldownsScreen(this, config)),
						null),
				new SmpCoreTileGrid.Entry(new ItemStack(Items.NOTE_BLOCK), Component.literal("Voice Chat"), Component.literal("Manage Simple Voice Chat server config (integration)."),
						List.of(Component.literal("Simple Voice Chat integration + server-side settings.")),
						() -> this.minecraft.setScreen(new SmpCoreVoiceChatScreen(this, config)),
						null),
				new SmpCoreTileGrid.Entry(new ItemStack(Items.CHEST), Component.literal("Kits"), Component.literal("First-join kit and other starter items."),
						List.of(Component.literal("Configure the first-join kit item list.")),
						() -> this.minecraft.setScreen(new SmpCoreKitsScreen(this, config)),
						null)
				,
				new SmpCoreTileGrid.Entry(new ItemStack(Items.KNOWLEDGE_BOOK), Component.literal("Recipes"), Component.literal("Custom crafting recipes (server datapack)."),
						List.of(Component.literal("Edit recipes, then run /smpcore recipes install on the server.")),
						() -> this.minecraft.setScreen(new SmpCoreRecipesScreen(this, config)),
						() -> Component.literal(config.recipes.enabled ? (config.recipes.shapeless.size() + " entries") : "Disabled")
				)
		)));

		int btnY = this.height - 32;
		int navW = 80;
		int gap = 8;

		prevPage = addRenderableWidget(new SmpCoreStyledButton(left, btnY, navW, 20, Component.literal("Prev"), () -> {
			if (grid != null) {
				grid.prevPage();
			}
		}));
		nextPage = addRenderableWidget(new SmpCoreStyledButton(left + navW + gap, btnY, navW, 20, Component.literal("Next"), () -> {
			if (grid != null) {
				grid.nextPage();
			}
		}));

		int closeX = left + navW * 2 + gap * 2;
		int closeW = Math.max(120, w - (closeX - left));
		addRenderableWidget(new SmpCoreStyledButton(closeX, btnY, closeW, 20, Component.literal("Close"), this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderSmpBackground(graphics);
		renderSmpHeader(graphics, Component.literal("SMP Core - Control Center"), Component.literal("Click a category to configure server rules"));
		super.render(graphics, mouseX, mouseY, partialTick);
		if (grid != null) {
			List<Component> tooltip = grid.consumeHoveredTooltip();
			if (tooltip != null) {
				graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
			}

			int pages = grid.pageCount();
			boolean showNav = pages > 1;
			if (prevPage != null) {
				prevPage.visible = showNav;
				prevPage.active = showNav;
			}
			if (nextPage != null) {
				nextPage.visible = showNav;
				nextPage.active = showNav;
			}
		}
	}
}
