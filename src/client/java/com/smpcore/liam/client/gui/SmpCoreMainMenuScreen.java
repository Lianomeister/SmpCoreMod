package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.IconCardButton;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SmpCoreMainMenuScreen extends SmpCoreMenuBase {
	public SmpCoreMainMenuScreen(SmpCoreConfig config) {
		super(Component.literal("SMP Core"), null, config);
	}

	@Override
	protected void init() {
		int w = 220;
		int h = 28;
		int gap = 10;

		int left = (this.width - w) / 2;
		int y = 44;

		addRenderableWidget(new IconCardButton(left, y, w, h, new ItemStack(Items.IRON_SWORD), Component.literal("Gameplay"), () -> {
			this.minecraft.setScreen(new SmpCoreGameplayScreen(this, config));
		}));
		y += h + gap;

		addRenderableWidget(new IconCardButton(left, y, w, h, new ItemStack(Items.BARRIER), Component.literal("Bans"), () -> {
			this.minecraft.setScreen(new SmpCoreBansScreen(this, config));
		}));
		y += h + gap;

		addRenderableWidget(new IconCardButton(left, y, w, h, new ItemStack(Items.POTION), Component.literal("Potions"), () -> {
			this.minecraft.setScreen(new SmpCorePotionsScreen(this, config));
		}));
		y += h + gap;

		addRenderableWidget(new IconCardButton(left, y, w, h, new ItemStack(Items.ENCHANTED_BOOK), Component.literal("Enchant Limits"), () -> {
			this.minecraft.setScreen(new SmpCoreEnchantmentsScreen(this, config));
		}));

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
	}
}

