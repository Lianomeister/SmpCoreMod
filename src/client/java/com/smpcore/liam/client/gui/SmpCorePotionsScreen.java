package com.smpcore.liam.client.gui;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class SmpCorePotionsScreen extends SmpCoreMenuBase {
	private MultiLineEditBox bannedEffectIds;
	private Button banAllButton;

	public SmpCorePotionsScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Potions"), parent, config);
	}

	@Override
	protected void init() {
		int w = 280;
		int x = (this.width - w) / 2;
		int y = 44;

		banAllButton = addRenderableWidget(Button.builder(banAllTitle(), b -> {
			config.potions.banAll = !config.potions.banAll;
			b.setMessage(banAllTitle());
			saveToServer();
		}).bounds(x, y, w, 20).build());
		y += 26;

		addRenderableWidget(Button.builder(Component.literal("Toggle common effects…"), b -> toggleCommonEffects()).bounds(x, y, w, 20).build());
		y += 30;

		bannedEffectIds = new MultiLineEditBox.Builder()
				.setX(x)
				.setY(y)
				.setShowBackground(true)
				.setPlaceholder(Component.literal("minecraft:strength\nminecraft:invisibility\n# one per line"))
				.build(font, w, 110, Component.literal("Banned potion effects"));
		bannedEffectIds.setValue(String.join("\n", config.potions.bannedPotionEffects));
		addRenderableWidget(bannedEffectIds);

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.literal("Ban all potions or specific effects used by potions"), width / 2, 30, 0xB9B9B9);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private void save() {
		config.potions.bannedPotionEffects = parseIdList(bannedEffectIds.getValue());
		saveToServer();
	}

	@Override
	public void onClose() {
		save();
		super.onClose();
	}

	private Component banAllTitle() {
		return Component.literal("All potions: " + (config.potions.banAll ? "Banned" : "Allowed"));
	}

	private void toggleCommonEffects() {
		toggleEffect("minecraft:strength");
		toggleEffect("minecraft:speed");
		toggleEffect("minecraft:invisibility");
		toggleEffect("minecraft:regeneration");
		toggleEffect("minecraft:poison");
		bannedEffectIds.setValue(String.join("\n", config.potions.bannedPotionEffects));
		saveToServer();
	}

	private void toggleEffect(String id) {
		if (config.potions.bannedPotionEffects.contains(id)) {
			config.potions.bannedPotionEffects.remove(id);
		} else {
			config.potions.bannedPotionEffects.add(id);
		}
	}

	private static List<String> parseIdList(String raw) {
		List<String> out = new ArrayList<>();
		for (String line : raw.split("\\R")) {
			String trimmed = line.trim();
			if (trimmed.isEmpty() || trimmed.startsWith("#")) {
				continue;
			}
			out.add(trimmed);
		}
		return out;
	}
}
