package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreDiagonalCarousel;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class SmpCoreMainMenuScreen extends SmpCoreMenuBase {
	private SmpCoreDiagonalCarousel carousel;

	public SmpCoreMainMenuScreen(SmpCoreConfig config) {
		super(Component.literal("SMP Core"), null, config);
	}

	@Override
	protected void init() {
		int w = Math.min(480, this.width - 40);
		int h = this.height - 92;
		int left = (this.width - w) / 2;
		int top = 44;

		this.carousel = addRenderableWidget(new SmpCoreDiagonalCarousel(left, top, w, h, List.of(
				new SmpCoreDiagonalCarousel.Entry(new ItemStack(Items.IRON_SWORD), Component.literal("Gameplay"), Component.literal("PvP, anti-xray, spectator after death, dimensions, sleep."),
						List.of(Component.literal("PvP toggle, anti-xray settings, spectator-after-death.")),
						() -> this.minecraft.setScreen(new SmpCoreGameplayScreen(this, config)),
						null),
				new SmpCoreDiagonalCarousel.Entry(new ItemStack(Items.BARRIER), Component.literal("Bans"), Component.literal("Bed/anchor/TNT minecart/mace/pearls/crystals/tipped arrows and more."),
						List.of(Component.literal("Bed/anchor/TNT minecart/mace/pearls/crystals/tipped arrows.")),
						() -> this.minecraft.setScreen(new SmpCoreBansScreen(this, config)),
						null),
				new SmpCoreDiagonalCarousel.Entry(new ItemStack(Items.POTION), Component.literal("Potions"), Component.literal("Ban all potions or only certain effects (with a sub-menu)."),
						List.of(Component.literal("Ban all potions or specific potion effects.")),
						() -> this.minecraft.setScreen(new SmpCorePotionsScreen(this, config)),
						null),
				new SmpCoreDiagonalCarousel.Entry(new ItemStack(Items.ENCHANTED_BOOK), Component.literal("Enchant Limits"), Component.literal("Clamp max levels and ban specific enchantments (server-side)."),
						List.of(Component.literal("Sharpness/protection clamping + banned enchant list (server-side).")),
						() -> this.minecraft.setScreen(new SmpCoreEnchantmentsScreen(this, config)),
						null),
				new SmpCoreDiagonalCarousel.Entry(new ItemStack(Items.SHIELD), Component.literal("Combat"), Component.literal("Combat tag, anti-restock, anti-elytra, and damage scaling."),
						List.of(Component.literal("Combat tag, anti-restock, anti-elytra, PvP damage scaling.")),
						() -> this.minecraft.setScreen(new SmpCoreCombatScreen(this, config)),
						null),
				new SmpCoreDiagonalCarousel.Entry(new ItemStack(Items.ENDER_PEARL), Component.literal("Cooldowns"), Component.literal("Pearl, E-gap, wind charge, mace and more."),
						List.of(Component.literal("Pearl/gap/wind-charge cooldowns.")),
						() -> this.minecraft.setScreen(new SmpCoreCooldownsScreen(this, config)),
						null),
				new SmpCoreDiagonalCarousel.Entry(new ItemStack(Items.NOTE_BLOCK), Component.literal("Voice Chat"), Component.literal("Manage Simple Voice Chat server config (integration)."),
						List.of(Component.literal("Simple Voice Chat integration + server-side settings.")),
						() -> this.minecraft.setScreen(new SmpCoreVoiceChatScreen(this, config)),
						null)
		)));

		addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
				.bounds(left, this.height - 32, w, 20)
				.build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, Component.literal("SMP Core - Control Center"), width / 2, 18, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.literal("Click a category to configure server rules"), width / 2, 30, 0xB9B9B9);
		super.render(graphics, mouseX, mouseY, partialTick);
		if (carousel != null) {
			List<Component> tooltip = carousel.consumeHoveredTooltip();
			if (tooltip != null) {
				graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
			}
		}
	}
}
