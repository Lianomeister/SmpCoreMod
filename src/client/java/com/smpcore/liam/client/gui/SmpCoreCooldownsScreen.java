package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreCategoryList;
import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class SmpCoreCooldownsScreen extends SmpCoreMenuBase {
	private SmpCoreCategoryList list;

	public SmpCoreCooldownsScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Cooldowns"), parent, config);
	}

	@Override
	protected void init() {
		int w = Math.min(420, this.width - 40);
		int left = (this.width - w) / 2;
		int top = 44;
		int listBottom = this.height - 44;

		list = addRenderableWidget(new SmpCoreCategoryList(this.minecraft, w, this.height, top, listBottom, 44));
		list.setLeftPos(left);

		addSeconds(new ItemStack(Items.ENDER_PEARL), "Pearl cooldown", "Cooldown for ender pearls.", () -> config.cooldowns.pearlSeconds, v -> config.cooldowns.pearlSeconds = v);
		addSeconds(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), "E-Gap cooldown", "Cooldown for enchanted golden apples.", () -> config.cooldowns.eGapSeconds, v -> config.cooldowns.eGapSeconds = v);
		addSeconds(new ItemStack(Items.WIND_CHARGE), "Wind charge cooldown", "Cooldown for wind charges.", () -> config.cooldowns.windChargeSeconds, v -> config.cooldowns.windChargeSeconds = v);
		addSeconds(new ItemStack(Items.MACE), "Mace cooldown", "Cooldown after the mace deals damage (blocked hits don't trigger it).", () -> config.cooldowns.maceSeconds, v -> config.cooldowns.maceSeconds = v);
		addSeconds(new ItemStack(Items.TRIDENT), "Riptide cooldown", "Cooldown for using Riptide tridents (applies when starting to use the trident).", () -> config.cooldowns.riptideSeconds, v -> config.cooldowns.riptideSeconds = v);

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.literal("Item and ability cooldowns"), width / 2, 30, 0xB9B9B9);
		super.render(graphics, mouseX, mouseY, partialTick);
		if (list != null) {
			List<Component> tooltip = list.consumeHoveredTooltip();
			if (tooltip != null) {
				graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
			}
		}
	}

	private void addSeconds(ItemStack icon, String title, String desc, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter) {
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				icon,
				Component.literal(title),
				Component.literal(desc),
				List.of(Component.literal(desc), Component.literal("0 = disabled")),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal(title),
						Component.literal("Enter seconds (0 disables)"),
						Integer.toString(getter.getAsInt()),
						List.of(Component.literal(desc), Component.literal("0 = disabled")),
						raw -> setter.accept(Math.max(0, parseInt(raw, getter.getAsInt())))
				)),
				() -> Component.literal(getter.getAsInt() + "s")
		));
	}

	private static int parseInt(String raw, int fallback) {
		try {
			return Integer.parseInt(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}
}
