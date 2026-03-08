package com.smpcore.liam.client.gui;

import com.smpcore.liam.config.ConfigJson;
import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.net.SmpCorePayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class SmpCoreAdminScreen extends Screen {
	private final SmpCoreConfig initial;

	private Checkbox banBedBombing;
	private Checkbox banAnchorBombing;
	private Checkbox banMace;
	private Checkbox banTntMinecarts;
	private Checkbox removeBannedItemsOnJoin;

	private Checkbox actionBar;
	private EditBox minMillisBetweenNotices;

	private EditBox effectsScanSeconds;
	private MultiLineEditBox bannedEffects;

	private EditBox enchScanSeconds;
	private Checkbox clampOnJoin;
	private EditBox sharpnessMax;
	private EditBox protectionMax;
	private MultiLineEditBox bannedEnchantments;

	private MultiLineEditBox bannedItems;

	public SmpCoreAdminScreen(SmpCoreConfig initial) {
		super(Component.literal("SMP Core - Admin Panel"));
		this.initial = initial;
	}

	@Override
	protected void init() {
		int contentWidth = Math.min(this.width - 40, 520);
		int left = (this.width - contentWidth) / 2;
		int y = 24;

		int colGap = 10;
		int colWidth = (contentWidth - colGap) / 2;
		int leftCol = left;
		int rightCol = left + colWidth + colGap;

		banBedBombing = addRenderableWidget(Checkbox.builder(Component.literal("Ban bed bombing"), font)
				.pos(leftCol, y).selected(initial.bans.banBedBombing).build());
		banAnchorBombing = addRenderableWidget(Checkbox.builder(Component.literal("Ban anchor bombing"), font)
				.pos(rightCol, y).selected(initial.bans.banAnchorBombing).build());
		y += 22;

		banMace = addRenderableWidget(Checkbox.builder(Component.literal("Ban mace"), font)
				.pos(leftCol, y).selected(initial.bans.banMace).build());
		banTntMinecarts = addRenderableWidget(Checkbox.builder(Component.literal("Ban TNT minecarts"), font)
				.pos(rightCol, y).selected(initial.bans.banTntMinecarts).build());
		y += 22;

		removeBannedItemsOnJoin = addRenderableWidget(Checkbox.builder(Component.literal("Remove banned items on join"), font)
				.pos(leftCol, y).selected(initial.bans.removeBannedItemsOnJoin).build());
		actionBar = addRenderableWidget(Checkbox.builder(Component.literal("Use actionbar notices"), font)
				.pos(rightCol, y).selected(initial.messages.actionBar).build());
		y += 26;

		minMillisBetweenNotices = addRenderableWidget(new EditBox(font, rightCol, y, colWidth, 20, Component.literal("Notice cooldown (ms)")));
		minMillisBetweenNotices.setValue(Long.toString(initial.messages.minMillisBetweenNotices));

		EditBox placeholder = new EditBox(font, leftCol, y, colWidth, 20, Component.literal(""));
		placeholder.setEditable(false);
		placeholder.setValue("Notice cooldown (ms):");
		addRenderableWidget(placeholder);
		y += 30;

		effectsScanSeconds = addRenderableWidget(new EditBox(font, leftCol, y, 90, 20, Component.literal("Effects scan (s)")));
		effectsScanSeconds.setValue(Integer.toString(initial.effects.scanSeconds));

		bannedEffects = new MultiLineEditBox.Builder()
				.setX(leftCol)
				.setY(y + 24)
				.setShowBackground(true)
				.build(font, colWidth, 88, Component.literal("Banned effects (one per line)"));
		bannedEffects.setValue(String.join("\n", initial.effects.bannedEffects));
		addRenderableWidget(bannedEffects);

		enchScanSeconds = addRenderableWidget(new EditBox(font, rightCol, y, 90, 20, Component.literal("Ench scan (s)")));
		enchScanSeconds.setValue(Integer.toString(initial.enchantments.scanSeconds));

		clampOnJoin = addRenderableWidget(Checkbox.builder(Component.literal("Clamp on join"), font)
				.pos(rightCol + 100, y + 2).selected(initial.enchantments.clampOnJoin).build());

		sharpnessMax = addRenderableWidget(new EditBox(font, rightCol, y + 24, 80, 20, Component.literal("Sharpness max")));
		sharpnessMax.setValue(Integer.toString(initial.enchantments.limits.sharpnessMax));

		protectionMax = addRenderableWidget(new EditBox(font, rightCol + 90, y + 24, 80, 20, Component.literal("Protection max")));
		protectionMax.setValue(Integer.toString(initial.enchantments.limits.protectionMax));

		bannedEnchantments = new MultiLineEditBox.Builder()
				.setX(rightCol)
				.setY(y + 48)
				.setShowBackground(true)
				.build(font, colWidth, 64, Component.literal("Banned enchantments (one per line)"));
		bannedEnchantments.setValue(String.join("\n", initial.enchantments.bannedEnchantments));
		addRenderableWidget(bannedEnchantments);

		y += 120;

		bannedItems = new MultiLineEditBox.Builder()
				.setX(leftCol)
				.setY(y)
				.setShowBackground(true)
				.build(font, contentWidth, 90, Component.literal("Banned items (one per line)"));
		bannedItems.setValue(String.join("\n", initial.bans.bannedItems));
		addRenderableWidget(bannedItems);

		y += 98;

		int buttonY = Math.min(this.height - 28, y);
		addRenderableWidget(Button.builder(Component.literal("Save"), btn -> save()).bounds(leftCol, buttonY, 120, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Close"), btn -> onClose()).bounds(leftCol + 130, buttonY, 120, 20).build());
	}

	private void save() {
		SmpCoreConfig cfg = new SmpCoreConfig();
		cfg.configVersion = initial.configVersion;

		cfg.messages.actionBar = actionBar.selected();
		cfg.messages.minMillisBetweenNotices = parseLong(minMillisBetweenNotices.getValue(), initial.messages.minMillisBetweenNotices);

		cfg.bans.banBedBombing = banBedBombing.selected();
		cfg.bans.banAnchorBombing = banAnchorBombing.selected();
		cfg.bans.banMace = banMace.selected();
		cfg.bans.banTntMinecarts = banTntMinecarts.selected();
		cfg.bans.removeBannedItemsOnJoin = removeBannedItemsOnJoin.selected();
		cfg.bans.bannedItems = parseIdList(bannedItems.getValue());

		cfg.effects.scanSeconds = parseInt(effectsScanSeconds.getValue(), initial.effects.scanSeconds);
		cfg.effects.bannedEffects = parseIdList(bannedEffects.getValue());

		cfg.enchantments.scanSeconds = parseInt(enchScanSeconds.getValue(), initial.enchantments.scanSeconds);
		cfg.enchantments.clampOnJoin = clampOnJoin.selected();
		cfg.enchantments.limits.sharpnessMax = parseInt(sharpnessMax.getValue(), initial.enchantments.limits.sharpnessMax);
		cfg.enchantments.limits.protectionMax = parseInt(protectionMax.getValue(), initial.enchantments.limits.protectionMax);
		cfg.enchantments.bannedEnchantments = parseIdList(bannedEnchantments.getValue());

		ClientPlayNetworking.send(new SmpCorePayloads.SaveConfigPayload(ConfigJson.toJson(cfg)));
		onClose();
	}

	private static int parseInt(String raw, int fallback) {
		try {
			return Integer.parseInt(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}

	private static long parseLong(String raw, long fallback) {
		try {
			return Long.parseLong(raw.trim());
		} catch (Exception ignored) {
			return fallback;
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
