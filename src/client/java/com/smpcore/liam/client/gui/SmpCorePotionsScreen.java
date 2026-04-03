package com.smpcore.liam.client.gui;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.client.gui.widget.SmpCoreStyledButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public final class SmpCorePotionsScreen extends SmpCoreMenuBase {
	private MultiLineEditBox bannedEffectIds;
	private SmpCoreStyledButton banAllButton;

	public SmpCorePotionsScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Potions"), parent, config);
	}

	@Override
	protected void init() {
		int w = 280;
		int x = (this.width - w) / 2;
		int y = 44;

		banAllButton = addRenderableWidget(new SmpCoreStyledButton(x, y, w, 20, banAllTitle(), new ItemStack(Items.POTION), () -> {
			config.potions.banAll = !config.potions.banAll;
			banAllButton.setMessage(banAllTitle());
			saveToServer();
		}));
		y += 26;

		addRenderableWidget(new SmpCoreStyledButton(x, y, w, 20, Component.literal("Toggle common effects…"), new ItemStack(Items.BOOK), this::toggleCommonEffects));
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
		renderSmpBackground(graphics);
		renderSmpHeader(graphics, getTitle(), Component.literal("Ban all potions or specific effects used by potions"));
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
