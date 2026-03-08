package com.smpcore.liam.client.gui;

import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public final class SmpCoreCooldownsScreen extends SmpCoreMenuBase {
	private EditBox pearlSeconds;
	private EditBox eGapSeconds;
	private EditBox windChargeSeconds;
	private EditBox maceSeconds;

	public SmpCoreCooldownsScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Cooldowns"), parent, config);
	}

	@Override
	protected void init() {
		int w = 260;
		int x = (this.width - w) / 2;
		int y = 52;

		pearlSeconds = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Pearl cooldown (s)")));
		pearlSeconds.setValue(Integer.toString(config.cooldowns.pearlSeconds));
		pearlSeconds.setTooltip(Tooltip.create(Component.literal("Cooldown for ender pearls.")));
		y += 26;

		eGapSeconds = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("E-Gap cooldown (s)")));
		eGapSeconds.setValue(Integer.toString(config.cooldowns.eGapSeconds));
		eGapSeconds.setTooltip(Tooltip.create(Component.literal("Cooldown for enchanted golden apples.")));
		y += 26;

		windChargeSeconds = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Wind charge cooldown (s)")));
		windChargeSeconds.setValue(Integer.toString(config.cooldowns.windChargeSeconds));
		windChargeSeconds.setTooltip(Tooltip.create(Component.literal("Cooldown for wind charges.")));
		y += 26;

		maceSeconds = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Mace cooldown (s)")));
		maceSeconds.setValue(Integer.toString(config.cooldowns.maceSeconds));
		maceSeconds.setTooltip(Tooltip.create(Component.literal("Cooldown applies only after the mace deals damage (blocked hits don't trigger it).")));

		addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose()).bounds(x, this.height - 32, 126, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Save"), b -> save()).bounds(x + 134, this.height - 32, 126, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private void save() {
		config.cooldowns.pearlSeconds = parseInt(pearlSeconds.getValue(), config.cooldowns.pearlSeconds);
		config.cooldowns.eGapSeconds = parseInt(eGapSeconds.getValue(), config.cooldowns.eGapSeconds);
		config.cooldowns.windChargeSeconds = parseInt(windChargeSeconds.getValue(), config.cooldowns.windChargeSeconds);
		config.cooldowns.maceSeconds = parseInt(maceSeconds.getValue(), config.cooldowns.maceSeconds);
		saveToServer();
	}

	private static int parseInt(String raw, int fallback) {
		try {
			return Integer.parseInt(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}
}
