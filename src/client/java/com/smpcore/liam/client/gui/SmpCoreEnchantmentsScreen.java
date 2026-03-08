package com.smpcore.liam.client.gui;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.client.gui.widget.SmpCoreStyledButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public final class SmpCoreEnchantmentsScreen extends SmpCoreMenuBase {
	private EditBox sharpnessMax;
	private EditBox protectionMax;
	private MultiLineEditBox bannedEnchantments;
	private EditBox scanSeconds;

	public SmpCoreEnchantmentsScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Enchant Limits"), parent, config);
	}

	@Override
	protected void init() {
		int w = 240;
		int x = (this.width - w) / 2;
		int y = 52;

		SmpCoreStyledButton[] clamp = new SmpCoreStyledButton[1];
		clamp[0] = addRenderableWidget(new SmpCoreStyledButton(x, y, w, 20,
				Component.literal("Clamp on join: " + (config.enchantments.clampOnJoin ? "Enabled" : "Disabled")),
				new ItemStack(Items.ANVIL),
				() -> {
					config.enchantments.clampOnJoin = !config.enchantments.clampOnJoin;
					clamp[0].setMessage(Component.literal("Clamp on join: " + (config.enchantments.clampOnJoin ? "Enabled" : "Disabled")));
					saveToServer();
				}));
		y += 30;

		addRenderableWidget(new SmpCoreStyledButton(x, y, w, 20,
				Component.literal("Toggle common bans…"),
				new ItemStack(Items.ENCHANTED_BOOK),
				this::toggleCommonBans));
		y += 26;

		sharpnessMax = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Sharpness max")));
		sharpnessMax.setValue(Integer.toString(config.enchantments.limits.sharpnessMax));
		y += 26;

		protectionMax = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Protection max")));
		protectionMax.setValue(Integer.toString(config.enchantments.limits.protectionMax));
		y += 26;

		bannedEnchantments = new MultiLineEditBox.Builder()
				.setX(x)
				.setY(y)
				.setShowBackground(true)
				.setPlaceholder(Component.literal("minecraft:breach\nminecraft:smite"))
				.build(font, w, 80, Component.literal("Banned enchantments"));
		bannedEnchantments.setValue(String.join("\n", config.enchantments.bannedEnchantments));
		addRenderableWidget(bannedEnchantments);
		y += 84;

		scanSeconds = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Scan interval (seconds)")));
		scanSeconds.setValue(Integer.toString(config.enchantments.scanSeconds));

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.literal("Set 0 to disable a limit and ban more enchantments"), width / 2, 30, 0xB9B9B9);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private void save() {
		config.enchantments.limits.sharpnessMax = parseInt(sharpnessMax.getValue(), config.enchantments.limits.sharpnessMax);
		config.enchantments.limits.protectionMax = parseInt(protectionMax.getValue(), config.enchantments.limits.protectionMax);
		config.enchantments.bannedEnchantments = parseIdList(bannedEnchantments.getValue());
		config.enchantments.scanSeconds = parseInt(scanSeconds.getValue(), config.enchantments.scanSeconds);
		saveToServer();
	}

	@Override
	public void onClose() {
		save();
		super.onClose();
	}

	private void toggleCommonBans() {
		toggleEnchantment("minecraft:breach");
		toggleEnchantment("minecraft:smite");
		toggleEnchantment("minecraft:fire_aspect");
		bannedEnchantments.setValue(String.join("\n", config.enchantments.bannedEnchantments));
		saveToServer();
	}

	private void toggleEnchantment(String id) {
		if (config.enchantments.bannedEnchantments.contains(id)) {
			config.enchantments.bannedEnchantments.remove(id);
		} else {
			config.enchantments.bannedEnchantments.add(id);
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
