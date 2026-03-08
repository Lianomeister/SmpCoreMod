package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreCategoryList;
import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class SmpCoreVoiceChatScreen extends SmpCoreMenuBase {
	private SmpCoreCategoryList list;

	public SmpCoreVoiceChatScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Voice Chat"), parent, config);
	}

	@Override
	protected void init() {
		int w = Math.min(420, this.width - 40);
		int left = (this.width - w) / 2;
		int top = 44;
		int listBottom = this.height - 44;

		list = addRenderableWidget(new SmpCoreCategoryList(this.minecraft, w, this.height, top, listBottom, 44));
		list.setLeftPos(left);

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.PLAYER_HEAD),
				Component.literal("Client settings"),
				Component.literal("Edit your local voicechat-client.properties (only affects your client)."),
				List.of(Component.literal("This does NOT change other players. Useful for admins/modpack testing.")),
				() -> this.minecraft.setScreen(new SmpCoreVoiceChatClientScreen(this, config)),
				() -> Component.literal("Open")
		));

		addToggle(
				new ItemStack(Items.COMPARATOR),
				"Manage Simple Voice Chat",
				"Write settings into config/voicechat/voicechat-server.properties.",
				List.of(Component.literal("If disabled, SMP Core won't touch Simple Voice Chat config files.")),
				() -> config.voiceChat.manageSimpleVoiceChat,
				v -> config.voiceChat.manageSimpleVoiceChat = v
		);

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.BOOK),
				Component.literal("Codec"),
				Component.literal("Audio codec mode used by voice chat."),
				List.of(Component.literal("Cycles Simple Voice Chat 'codec' property.")),
				() -> {
					config.voiceChat.codec = next(config.voiceChat.codec);
					saveToServer();
				},
				() -> Component.literal(config.voiceChat.codec.name())
		));

		addEditInt(
				new ItemStack(Items.REPEATER),
				"Port",
				"UDP port (24454 default). Use -1 to disable the UDP server.",
				() -> config.voiceChat.port,
				v -> config.voiceChat.port = v,
				-1, 65535
		);

		addEditString(
				new ItemStack(Items.NAME_TAG),
				"Bind address",
				"IP/interface to bind to (empty = all).",
				() -> config.voiceChat.bindAddress,
				v -> config.voiceChat.bindAddress = v
		);

		addEditString(
				new ItemStack(Items.ENDER_EYE),
				"Voice host",
				"Public hostname/IP for clients (empty = auto).",
				() -> config.voiceChat.voiceHost,
				v -> config.voiceChat.voiceHost = v
		);

		addEditDouble(
				new ItemStack(Items.COMPASS),
				"Max voice distance",
				"How far players can be heard (blocks).",
				() -> config.voiceChat.maxVoiceDistance,
				v -> config.voiceChat.maxVoiceDistance = v,
				0.0, 1024.0
		);

		addEditDouble(
				new ItemStack(Items.FEATHER),
				"Whisper distance",
				"Whisper range (blocks).",
				() -> config.voiceChat.whisperDistance,
				v -> config.voiceChat.whisperDistance = v,
				0.0, 1024.0
		);

		addEditDouble(
				new ItemStack(Items.STRUCTURE_VOID),
				"Broadcast range",
				"Range where players are always audible (-1 = max voice distance).",
				() -> config.voiceChat.broadcastRange,
				v -> config.voiceChat.broadcastRange = v,
				-1.0, 1024.0
		);

		addEditInt(
				new ItemStack(Items.STRING),
				"MTU size",
				"Packet MTU size (bytes). Lower can help with bad networks.",
				() -> config.voiceChat.mtuSize,
				v -> config.voiceChat.mtuSize = v,
				64, 65535
		);

		addEditInt(
				new ItemStack(Items.CLOCK),
				"Keep alive",
				"Keep-alive interval (ms).",
				() -> config.voiceChat.keepAliveMs,
				v -> config.voiceChat.keepAliveMs = v,
				100, 60_000
		);

		addToggle(
				new ItemStack(Items.PAPER),
				"Groups",
				"Enable/disable voice chat groups.",
				List.of(Component.literal("Toggles Simple Voice Chat 'enable_groups'.")),
				() -> config.voiceChat.enableGroups,
				v -> config.voiceChat.enableGroups = v
		);

		addToggle(
				new ItemStack(Items.WRITABLE_BOOK),
				"Recording",
				"Allow recording audio in groups.",
				List.of(Component.literal("Toggles Simple Voice Chat 'allow_recording'.")),
				() -> config.voiceChat.allowRecording,
				v -> config.voiceChat.allowRecording = v
		);

		addToggle(
				new ItemStack(Items.SPYGLASS),
				"Spectator interaction",
				"Let spectators hear/talk (depending on voice chat behavior).",
				List.of(Component.literal("Toggles Simple Voice Chat 'spectator_interaction'.")),
				() -> config.voiceChat.spectatorInteraction,
				v -> config.voiceChat.spectatorInteraction = v
		);

		addToggle(
				new ItemStack(Items.ENDER_PEARL),
				"Spectator possession",
				"Let spectators possess players for voice position.",
				List.of(Component.literal("Toggles Simple Voice Chat 'spectator_player_possession'.")),
				() -> config.voiceChat.spectatorPlayerPossession,
				v -> config.voiceChat.spectatorPlayerPossession = v
		);

		addToggle(
				new ItemStack(Items.BARRIER),
				"Force voice chat",
				"Require voice chat to join/play (mod dependent).",
				List.of(Component.literal("Toggles Simple Voice Chat 'force_voice_chat'.")),
				() -> config.voiceChat.forceVoiceChat,
				v -> config.voiceChat.forceVoiceChat = v
		);

		addToggle(
				new ItemStack(Items.BELL),
				"Allow pings",
				"Allow voice chat pings/notifications.",
				List.of(Component.literal("Toggles Simple Voice Chat 'allow_pings'.")),
				() -> config.voiceChat.allowPings,
				v -> config.voiceChat.allowPings = v
		);

		addToggle(
				new ItemStack(Items.IRON_INGOT),
				"Use natives",
				"Enable native audio processing if available.",
				List.of(Component.literal("Toggles Simple Voice Chat 'use_natives'.")),
				() -> config.voiceChat.useNatives,
				v -> config.voiceChat.useNatives = v
		);

		addEditInt(
				new ItemStack(Items.TRIPWIRE_HOOK),
				"Login timeout",
				"Timeout for voice chat handshake (ms).",
				() -> config.voiceChat.loginTimeoutMs,
				v -> config.voiceChat.loginTimeoutMs = v,
				1000, 120_000
		);

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.literal("Simple Voice Chat integration (server settings)"), width / 2, 30, 0xB9B9B9);
		super.render(graphics, mouseX, mouseY, partialTick);
		if (list != null) {
			List<Component> tooltip = list.consumeHoveredTooltip();
			if (tooltip != null) {
				graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
			}
		}
	}

	private void addToggle(ItemStack icon, String title, String desc, List<Component> tooltip, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				icon,
				Component.literal(title),
				Component.literal(desc),
				tooltip,
				() -> {
					boolean next = !getter.getAsBoolean();
					setter.accept(next);
					saveToServer();
				},
				() -> Component.literal(getter.getAsBoolean() ? "Enabled" : "Disabled")
		));
	}

	private void addEditString(ItemStack icon, String title, String desc, java.util.function.Supplier<String> getter, java.util.function.Consumer<String> setter) {
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				icon,
				Component.literal(title),
				Component.literal(desc),
				List.of(Component.literal(desc)),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal(title),
						Component.literal("Enter a value"),
						getter.get(),
						List.of(Component.literal(desc)),
						raw -> {
							setter.accept(raw == null ? "" : raw.trim());
						})),
				() -> Component.literal(safe(getter.get()))
		));
	}

	private void addEditInt(ItemStack icon, String title, String desc, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter, int min, int max) {
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				icon,
				Component.literal(title),
				Component.literal(desc),
				List.of(Component.literal(desc), Component.literal("Range: " + min + " .. " + max)),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal(title),
						Component.literal("Enter a number"),
						Integer.toString(getter.getAsInt()),
						List.of(Component.literal(desc), Component.literal("Range: " + min + " .. " + max)),
						raw -> setter.accept(clamp(parseInt(raw, getter.getAsInt()), min, max)))),
				() -> Component.literal(Integer.toString(getter.getAsInt()))
		));
	}

	private void addEditDouble(ItemStack icon, String title, String desc, java.util.function.DoubleSupplier getter, java.util.function.DoubleConsumer setter, double min, double max) {
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				icon,
				Component.literal(title),
				Component.literal(desc),
				List.of(Component.literal(desc), Component.literal("Range: " + min + " .. " + max)),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal(title),
						Component.literal("Enter a number"),
						Double.toString(getter.getAsDouble()),
						List.of(Component.literal(desc), Component.literal("Range: " + min + " .. " + max)),
						raw -> setter.accept(clamp(parseDouble(raw, getter.getAsDouble()), min, max)))),
				() -> Component.literal(trimDouble(getter.getAsDouble()))
		));
	}

	private static String safe(String raw) {
		if (raw == null || raw.isBlank()) {
			return "(empty)";
		}
		return raw.length() > 24 ? raw.substring(0, 24) + "…" : raw;
	}

	private static String trimDouble(double v) {
		String s = Double.toString(v);
		return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
	}

	private static int clamp(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}

	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
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

	private static SmpCoreConfig.VoiceChatCodec next(SmpCoreConfig.VoiceChatCodec codec) {
		SmpCoreConfig.VoiceChatCodec[] values = SmpCoreConfig.VoiceChatCodec.values();
		return values[(codec.ordinal() + 1) % values.length];
	}
}
