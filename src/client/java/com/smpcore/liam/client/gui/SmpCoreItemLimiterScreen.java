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

public final class SmpCoreItemLimiterScreen extends SmpCoreMenuBase {
	private SmpCoreStyledButton enabledButton;
	private SmpCoreStyledButton preventPickupButton;
	private SmpCoreStyledButton dropExcessButton;
	private EditBox scanSeconds;
	private MultiLineEditBox limits;

	public SmpCoreItemLimiterScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Item Limiter"), parent, config);
	}

	@Override
	protected void init() {
		int w = 340;
		int x = (this.width - w) / 2;
		int y = 44;

		enabledButton = addRenderableWidget(new SmpCoreStyledButton(x, y, w, 20, enabledTitle(), new ItemStack(Items.HOPPER), () -> {
			config.itemLimiter.enabled = !config.itemLimiter.enabled;
			enabledButton.setMessage(enabledTitle());
			saveToServer();
		}));
		y += 26;

		preventPickupButton = addRenderableWidget(new SmpCoreStyledButton(x, y, w, 20, preventPickupTitle(), new ItemStack(Items.MINECART), () -> {
			config.itemLimiter.preventPickup = !config.itemLimiter.preventPickup;
			preventPickupButton.setMessage(preventPickupTitle());
			saveToServer();
		}));
		y += 26;

		dropExcessButton = addRenderableWidget(new SmpCoreStyledButton(x, y, w, 20, dropExcessTitle(), new ItemStack(Items.DROPPER), () -> {
			config.itemLimiter.dropExcess = !config.itemLimiter.dropExcess;
			dropExcessButton.setMessage(dropExcessTitle());
			saveToServer();
		}));
		y += 30;

		scanSeconds = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Scan interval (seconds)")));
		scanSeconds.setValue(Integer.toString(config.itemLimiter.scanSeconds));
		y += 28;

		limits = new MultiLineEditBox.Builder()
				.setX(x)
				.setY(y)
				.setShowBackground(true)
				.setPlaceholder(Component.literal(
						"# one per line: <item_id> <max>\n" +
						"minecraft:ender_pearl 16\n" +
						"minecraft:totem_of_undying 1"
				))
				.build(font, w, 118, Component.literal("Item limits"));
		limits.setValue(formatLimits(config.itemLimiter.limits));
		addRenderableWidget(limits);

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderSmpBackground(graphics);
		renderSmpHeader(graphics, getTitle(), Component.literal("Limit items per player inventory"));
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void onClose() {
		save();
		super.onClose();
	}

	private void save() {
		config.itemLimiter.scanSeconds = parseInt(scanSeconds.getValue(), config.itemLimiter.scanSeconds);
		config.itemLimiter.limits = parseLimits(limits.getValue());
		saveToServer();
	}

	private Component enabledTitle() {
		return Component.literal("Item limiter: " + (config.itemLimiter.enabled ? "Enabled" : "Disabled"));
	}

	private Component preventPickupTitle() {
		return Component.literal("Prevent pickup: " + (config.itemLimiter.preventPickup ? "Yes" : "No"));
	}

	private Component dropExcessTitle() {
		return Component.literal("Drop excess items: " + (config.itemLimiter.dropExcess ? "Yes" : "No"));
	}

	private static int parseInt(String raw, int fallback) {
		try {
			return Integer.parseInt(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}

	private static String formatLimits(List<SmpCoreConfig.ItemLimit> limits) {
		if (limits == null || limits.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (SmpCoreConfig.ItemLimit l : limits) {
			if (l == null || l.id == null || l.id.isBlank()) {
				continue;
			}
			int max = Math.max(0, l.max);
			sb.append(l.id.trim()).append(' ').append(max).append('\n');
		}
		return sb.toString().trim();
	}

	private static List<SmpCoreConfig.ItemLimit> parseLimits(String raw) {
		ArrayList<SmpCoreConfig.ItemLimit> out = new ArrayList<>();
		if (raw == null || raw.isBlank()) {
			return out;
		}
		for (String line : raw.split("\\R")) {
			String trimmed = line.trim();
			if (trimmed.isEmpty() || trimmed.startsWith("#")) {
				continue;
			}
			String[] parts = trimmed.split("\\s+");
			if (parts.length < 2) {
				continue;
			}
			String id = parts[0].trim();
			if (id.isEmpty()) {
				continue;
			}
			int max;
			try {
				max = Integer.parseInt(parts[1].trim());
			} catch (Exception ignored) {
				continue;
			}
			SmpCoreConfig.ItemLimit l = new SmpCoreConfig.ItemLimit();
			l.id = id;
			l.max = Math.max(0, max);
			out.add(l);
		}
		return out;
	}
}

