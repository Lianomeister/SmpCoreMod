package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.client.gui.widget.SmpCoreStyledButton;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public final class SmpCoreKitsScreen extends SmpCoreMenuBase {
	private SmpCoreStyledButton enabledButton;
	private SmpCoreStyledButton onlyOnceButton;
	private MultiLineEditBox items;

	public SmpCoreKitsScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Kits"), parent, config);
	}

	@Override
	protected void init() {
		int w = 320;
		int x = (this.width - w) / 2;
		int y = 44;

		enabledButton = addRenderableWidget(new SmpCoreStyledButton(
				x, y, w, 20,
				enabledTitle(),
				new ItemStack(Items.CHEST),
				() -> {
					config.kits.firstJoin.enabled = !config.kits.firstJoin.enabled;
					enabledButton.setMessage(enabledTitle());
					saveToServer();
				}
		));
		y += 26;

		onlyOnceButton = addRenderableWidget(new SmpCoreStyledButton(
				x, y, w, 20,
				onlyOnceTitle(),
				new ItemStack(Items.NAME_TAG),
				() -> {
					config.kits.firstJoin.onlyOnce = !config.kits.firstJoin.onlyOnce;
					onlyOnceButton.setMessage(onlyOnceTitle());
					saveToServer();
				}
		));
		y += 30;

		items = new MultiLineEditBox.Builder()
				.setX(x)
				.setY(y)
				.setShowBackground(true)
				.setPlaceholder(Component.literal(
						"# one per line: <item_id> <count>\n" +
						"minecraft:bread 16\n" +
						"minecraft:stone_sword 1"
				))
				.build(font, w, 118, Component.literal("First-join kit items"));
		items.setValue(formatItems(config.kits.firstJoin.items));
		addRenderableWidget(items);
		y += 122;

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderSmpBackground(graphics);
		renderSmpHeader(graphics, getTitle(), Component.literal("First-join kit and other starter items"));
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void onClose() {
		save();
		super.onClose();
	}

	private void save() {
		config.kits.firstJoin.items = parseItems(items.getValue());
		saveToServer();
	}

	private Component enabledTitle() {
		return Component.literal("First-join kit: " + (config.kits.firstJoin.enabled ? "Enabled" : "Disabled"));
	}

	private Component onlyOnceTitle() {
		return Component.literal("Grant only once: " + (config.kits.firstJoin.onlyOnce ? "Yes" : "No"));
	}

	private static String formatItems(List<SmpCoreConfig.KitItem> items) {
		if (items == null || items.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (SmpCoreConfig.KitItem it : items) {
			if (it == null || it.id == null || it.id.isBlank()) {
				continue;
			}
			int count = Math.max(1, it.count);
			sb.append(it.id.trim()).append(' ').append(count).append('\n');
		}
		return sb.toString().trim();
	}

	private static List<SmpCoreConfig.KitItem> parseItems(String raw) {
		ArrayList<SmpCoreConfig.KitItem> out = new ArrayList<>();
		if (raw == null || raw.isBlank()) {
			return out;
		}
		for (String line : raw.split("\\R")) {
			String trimmed = line.trim();
			if (trimmed.isEmpty() || trimmed.startsWith("#")) {
				continue;
			}
			trimmed = trimmed.replace(" x", " ").replace("\tx", "\t");
			String[] parts = trimmed.split("\\s+");
			if (parts.length == 0) {
				continue;
			}
			String id = parts[0].trim();
			if (id.isEmpty()) {
				continue;
			}
			int count = 1;
			if (parts.length >= 2) {
				try {
					count = Integer.parseInt(parts[1].trim());
				} catch (Exception ignored) {
				}
			}
			SmpCoreConfig.KitItem item = new SmpCoreConfig.KitItem();
			item.id = id;
			item.count = Math.max(1, count);
			out.add(item);
		}
		return out;
	}
}

