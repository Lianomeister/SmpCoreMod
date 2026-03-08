package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.client.gui.widget.SmpCoreStyledButton;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public final class SmpCoreEffectsScreen extends SmpCoreMenuBase {
	private MultiLineEditBox bannedEffects;
	private EditBox scanSeconds;

	public SmpCoreEffectsScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Effect bans"), parent, config);
	}

	@Override
	protected void init() {
		int w = 280;
		int x = (this.width - w) / 2;
		int y = 44;

		addRenderableWidget(new SmpCoreStyledButton(x, y, w, 20, Component.literal("Toggle indicator effects…"), new ItemStack(Items.GLOWSTONE_DUST), this::toggleIndicatorEffects));
		y += 26;

		bannedEffects = new MultiLineEditBox.Builder()
				.setX(x)
				.setY(y)
				.setShowBackground(true)
				.setPlaceholder(Component.literal("minecraft:health_boost\nminecraft:absorption"))
				.build(font, w, 110, Component.literal("Banned effects"));
		bannedEffects.setValue(String.join("\n", config.effects.bannedEffects));
		addRenderableWidget(bannedEffects);

		y += 114;
		scanSeconds = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Scan interval (seconds)")));
		scanSeconds.setValue(Integer.toString(config.effects.scanSeconds));
		y += 26;

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void onClose() {
		save();
		super.onClose();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.literal("Ban lingering effects such as health indicators"), width / 2, 30, 0xB9B9B9);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private void save() {
		config.effects.bannedEffects = parseIdList(bannedEffects.getValue());
		config.effects.scanSeconds = parseInt(scanSeconds.getValue(), config.effects.scanSeconds);
		saveToServer();
	}

	private void toggleIndicatorEffects() {
		toggleEffect("minecraft:health_boost");
		toggleEffect("minecraft:absorption");
		toggleEffect("minecraft:glowing");
		bannedEffects.setValue(String.join("\n", config.effects.bannedEffects));
		saveToServer();
	}

	private void toggleEffect(String id) {
		if (config.effects.bannedEffects.contains(id)) {
			config.effects.bannedEffects.remove(id);
		} else {
			config.effects.bannedEffects.add(id);
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

	private static int parseInt(String raw, int fallback) {
		try {
			return Integer.parseInt(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}
}
