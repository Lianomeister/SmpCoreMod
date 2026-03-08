package com.smpcore.liam.client.gui;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public final class SmpCoreEnchantmentsScreen extends SmpCoreMenuBase {
	private EditBox sharpnessMax;
	private EditBox protectionMax;

	public SmpCoreEnchantmentsScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Enchant Limits"), parent, config);
	}

	@Override
	protected void init() {
		int w = 240;
		int x = (this.width - w) / 2;
		int y = 52;

		addRenderableWidget(Button.builder(Component.literal("Clamp on join: " + (config.enchantments.clampOnJoin ? "Enabled" : "Disabled")), b -> {
			config.enchantments.clampOnJoin = !config.enchantments.clampOnJoin;
			b.setMessage(Component.literal("Clamp on join: " + (config.enchantments.clampOnJoin ? "Enabled" : "Disabled")));
			saveToServer();
		}).bounds(x, y, w, 20).build());
		y += 30;

		sharpnessMax = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Sharpness max")));
		sharpnessMax.setValue(Integer.toString(config.enchantments.limits.sharpnessMax));
		y += 26;

		protectionMax = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Protection max")));
		protectionMax.setValue(Integer.toString(config.enchantments.limits.protectionMax));

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.literal("Set 0 to disable a limit"), width / 2, 30, 0xB9B9B9);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private void save() {
		config.enchantments.limits.sharpnessMax = parseInt(sharpnessMax.getValue(), config.enchantments.limits.sharpnessMax);
		config.enchantments.limits.protectionMax = parseInt(protectionMax.getValue(), config.enchantments.limits.protectionMax);
		saveToServer();
	}

	@Override
	public void onClose() {
		save();
		super.onClose();
	}

	private static int parseInt(String raw, int fallback) {
		try {
			return Integer.parseInt(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}
}
