package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.integration.SimpleVoiceChatClientConfig;
import com.smpcore.liam.client.gui.widget.SmpCoreCategoryList;
import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public final class SmpCoreVoiceChatClientScreen extends SmpCoreMenuBase {
	private SmpCoreCategoryList list;
	private Properties props;

	public SmpCoreVoiceChatClientScreen(Screen parent, SmpCoreConfig config) {
		super(Component.literal("Voice Chat (Client)"), parent, config);
	}

	@Override
	protected void init() {
		this.props = SimpleVoiceChatClientConfig.loadOrEmpty();

		int w = Math.min(420, this.width - 40);
		int left = (this.width - w) / 2;
		int top = 44;
		int listBottom = this.height - 44;

		list = addRenderableWidget(new SmpCoreCategoryList(this.minecraft, w, this.height, top, listBottom, 44));
		list.setLeftPos(left);

		addToggle(
				new ItemStack(Items.BARRIER),
				"Voice Chat disabled",
				"Disables voice chat on this client.",
				"disabled",
				true
		);

		addEditDouble(
				new ItemStack(Items.NOTE_BLOCK),
				"Voice chat volume",
				"Client-side volume multiplier (1.0 = default).",
				"voice_chat_volume",
				1.0,
				0.0, 2.0
		);

		addCycle(
				new ItemStack(Items.TRIPWIRE_HOOK),
				"Mic activation type",
				"Push-to-talk or voice activation.",
				"microphone_activation_type",
				"PTT",
				List.of("PTT", "VOICE")
		);

		addToggle(
				new ItemStack(Items.SPYGLASS),
				"Voice activity detection",
				"Detect speech automatically (recommended for VOICE mode).",
				"voice_activity_detection",
				true
		);

		addEditDouble(
				new ItemStack(Items.REDSTONE),
				"Activation threshold",
				"Threshold for voice activation detection.",
				"voice_activation_threshold",
				-50.0,
				-100.0, 0.0
		);

		addEditDouble(
				new ItemStack(Items.ANVIL),
				"Microphone gain",
				"Extra mic gain (can cause clipping).",
				"microphone_gain",
				1.0,
				0.0, 10.0
		);

		addToggle(
				new ItemStack(Items.SNOWBALL),
				"Denoiser",
				"Noise suppression (if supported).",
				"denoiser",
				true
		);

		addToggle(
				new ItemStack(Items.IRON_INGOT),
				"Use natives",
				"Enable native audio processing if available.",
				"use_natives",
				true
		);

		addToggle(
				new ItemStack(Items.ENDER_PEARL),
				"Run local server",
				"Runs a local voice chat server (singleplayer/LAN).",
				"run_local_server",
				false
		);

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.PAPER),
				Component.literal("Config file path"),
				Component.literal("Click to copy the voicechat-client.properties path."),
				List.of(Component.literal(SimpleVoiceChatClientConfig.clientConfigPath().toAbsolutePath().toString())),
				() -> Minecraft.getInstance().keyboardHandler.setClipboard(SimpleVoiceChatClientConfig.clientConfigPath().toAbsolutePath().toString()),
				() -> Component.literal("Copy")
		));

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderSmpBackground(graphics);
		renderSmpHeader(graphics, getTitle(), Component.literal("Local settings (config/voicechat/voicechat-client.properties)"));
		super.render(graphics, mouseX, mouseY, partialTick);
		if (list != null) {
			List<Component> tooltip = list.consumeHoveredTooltip();
			if (tooltip != null) {
				graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
			}
		}
	}

	private void addToggle(ItemStack icon, String title, String desc, String key, boolean defaultValue) {
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				icon,
				Component.literal(title),
				Component.literal(desc),
				List.of(Component.literal(desc), Component.literal("Key: " + key)),
				() -> {
					boolean next = !getBool(key, defaultValue);
					props.setProperty(key, Boolean.toString(next));
					saveProps();
				},
				() -> Component.literal(getBool(key, defaultValue) ? "Enabled" : "Disabled")
		));
	}

	private void addEditDouble(ItemStack icon, String title, String desc, String key, double defaultValue, double min, double max) {
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				icon,
				Component.literal(title),
				Component.literal(desc),
				List.of(Component.literal(desc), Component.literal("Key: " + key)),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal(title),
						Component.literal("Enter a number"),
						Double.toString(getDouble(key, defaultValue)),
						List.of(Component.literal(desc), Component.literal("Range: " + min + " .. " + max), Component.literal("Key: " + key)),
						raw -> {
							double v = clamp(parseDouble(raw, getDouble(key, defaultValue)), min, max);
							props.setProperty(key, Double.toString(v));
							saveProps();
						},
						false
				)),
				() -> Component.literal(trimDouble(getDouble(key, defaultValue)))
		));
	}

	private void addCycle(ItemStack icon, String title, String desc, String key, String defaultValue, List<String> values) {
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				icon,
				Component.literal(title),
				Component.literal(desc),
				List.of(Component.literal(desc), Component.literal("Key: " + key)),
				() -> {
					String cur = getString(key, defaultValue).toUpperCase(Locale.ROOT);
					int idx = values.indexOf(cur);
					String next = values.get((idx + 1) % values.size());
					props.setProperty(key, next);
					saveProps();
				},
				() -> Component.literal(getString(key, defaultValue).toUpperCase(Locale.ROOT))
		));
	}

	private void saveProps() {
		try {
			SimpleVoiceChatClientConfig.save(props);
		} catch (IOException ignored) {
		}
	}

	private boolean getBool(String key, boolean fallback) {
		String raw = props.getProperty(key);
		if (raw == null) {
			return fallback;
		}
		return Boolean.parseBoolean(raw.trim());
	}

	private double getDouble(String key, double fallback) {
		String raw = props.getProperty(key);
		if (raw == null) {
			return fallback;
		}
		try {
			return Double.parseDouble(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}

	private String getString(String key, String fallback) {
		String raw = props.getProperty(key);
		if (raw == null) {
			return fallback;
		}
		return raw.trim();
	}

	private static String trimDouble(double v) {
		String s = Double.toString(v);
		return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
	}

	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}

	private static double parseDouble(String raw, double fallback) {
		try {
			return Double.parseDouble(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}
}
