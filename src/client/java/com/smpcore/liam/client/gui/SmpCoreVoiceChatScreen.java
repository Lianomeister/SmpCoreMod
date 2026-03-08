package com.smpcore.liam.client.gui;

import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public final class SmpCoreVoiceChatScreen extends SmpCoreMenuBase {
	private EditBox port;
	private EditBox bindAddress;
	private EditBox maxDistance;
	private EditBox whisperDistance;
	private EditBox loginTimeout;

	private Button codec;
	private Button manage;
	private Button enableGroups;
	private Button allowRecording;
	private Button spectatorInteraction;
	private Button forceVoiceChat;

	public SmpCoreVoiceChatScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Voice Chat"), parent, config);
	}

	@Override
	protected void init() {
		int w = 280;
		int x = (this.width - w) / 2;
		int y = 44;

		manage = addRenderableWidget(Button.builder(manageTitle(), b -> {
			config.voiceChat.manageSimpleVoiceChat = !config.voiceChat.manageSimpleVoiceChat;
			b.setMessage(manageTitle());
			saveToServer();
		}).bounds(x, y, w, 20).build());
		manage.setTooltip(Tooltip.create(Component.literal("If enabled, SMP Core writes Simple Voice Chat server config file.")));
		y += 26;

		codec = addRenderableWidget(Button.builder(codecTitle(), b -> {
			config.voiceChat.codec = next(config.voiceChat.codec);
			b.setMessage(codecTitle());
			saveToServer();
		}).bounds(x, y, w, 20).build());
		codec.setTooltip(Tooltip.create(Component.literal("Audio codec mode (matches Simple Voice Chat server config).")));
		y += 26;

		port = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Port")));
		port.setValue(Integer.toString(config.voiceChat.port));
		port.setTooltip(Tooltip.create(Component.literal("UDP port used by voice chat (default 24454).")));
		y += 24;

		bindAddress = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Bind address")));
		bindAddress.setValue(config.voiceChat.bindAddress == null ? "" : config.voiceChat.bindAddress);
		bindAddress.setTooltip(Tooltip.create(Component.literal("Optional: IP to bind to (empty = all).")));
		y += 24;

		maxDistance = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Max voice distance")));
		maxDistance.setValue(Double.toString(config.voiceChat.maxVoiceDistance));
		maxDistance.setTooltip(Tooltip.create(Component.literal("How far players can be heard (blocks).")));
		y += 24;

		whisperDistance = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Whisper distance")));
		whisperDistance.setValue(Double.toString(config.voiceChat.whisperDistance));
		whisperDistance.setTooltip(Tooltip.create(Component.literal("Whisper range (blocks).")));
		y += 28;

		enableGroups = addRenderableWidget(toggle(x, y, w, "Groups", () -> config.voiceChat.enableGroups, v -> config.voiceChat.enableGroups = v));
		enableGroups.setTooltip(Tooltip.create(Component.literal("Enable/disable voice chat groups.")));
		y += 22;

		allowRecording = addRenderableWidget(toggle(x, y, w, "Recording", () -> config.voiceChat.allowRecording, v -> config.voiceChat.allowRecording = v));
		allowRecording.setTooltip(Tooltip.create(Component.literal("Allow recording audio in groups.")));
		y += 22;

		spectatorInteraction = addRenderableWidget(toggle(x, y, w, "Spectator interaction", () -> config.voiceChat.spectatorInteraction, v -> config.voiceChat.spectatorInteraction = v));
		y += 22;

		forceVoiceChat = addRenderableWidget(toggle(x, y, w, "Force voice chat", () -> config.voiceChat.forceVoiceChat, v -> config.voiceChat.forceVoiceChat = v));
		forceVoiceChat.setTooltip(Tooltip.create(Component.literal("If enabled, players without voice chat may be blocked (depends on Simple Voice Chat).")));
		y += 26;

		loginTimeout = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Login timeout (ms)")));
		loginTimeout.setValue(Integer.toString(config.voiceChat.loginTimeoutMs));
		loginTimeout.setTooltip(Tooltip.create(Component.literal("Timeout for voice chat login/handshake.")));

		addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose()).bounds(x, this.height - 32, 136, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Save"), b -> save()).bounds(x + 144, this.height - 32, 136, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.literal("Integrates with Simple Voice Chat (server config)"), width / 2, 30, 0xB9B9B9);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private void save() {
		config.voiceChat.port = parseInt(port.getValue(), config.voiceChat.port);
		config.voiceChat.bindAddress = bindAddress.getValue();
		config.voiceChat.maxVoiceDistance = parseDouble(maxDistance.getValue(), config.voiceChat.maxVoiceDistance);
		config.voiceChat.whisperDistance = parseDouble(whisperDistance.getValue(), config.voiceChat.whisperDistance);
		config.voiceChat.loginTimeoutMs = parseInt(loginTimeout.getValue(), config.voiceChat.loginTimeoutMs);
		saveToServer();
	}

	private Component manageTitle() {
		return Component.literal("Manage Simple Voice Chat config: " + (config.voiceChat.manageSimpleVoiceChat ? "Yes" : "No"));
	}

	private Component codecTitle() {
		return Component.literal("Codec: " + config.voiceChat.codec.name());
	}

	private Button toggle(int x, int y, int w, String label, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
		return Button.builder(Component.literal(label + ": " + (getter.getAsBoolean() ? "Enabled" : "Disabled")), b -> {
			boolean next = !getter.getAsBoolean();
			setter.accept(next);
			b.setMessage(Component.literal(label + ": " + (next ? "Enabled" : "Disabled")));
			saveToServer();
		}).bounds(x, y, w, 20).build();
	}

	private static SmpCoreConfig.VoiceChatCodec next(SmpCoreConfig.VoiceChatCodec codec) {
		SmpCoreConfig.VoiceChatCodec[] values = SmpCoreConfig.VoiceChatCodec.values();
		return values[(codec.ordinal() + 1) % values.length];
	}

	private static int parseInt(String raw, int fallback) {
		try {
			return Integer.parseInt(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}

	private static double parseDouble(String raw, double fallback) {
		try {
			return Double.parseDouble(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}
}

