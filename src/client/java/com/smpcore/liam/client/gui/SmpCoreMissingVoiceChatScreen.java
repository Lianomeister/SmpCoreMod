package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreStyledButton;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SmpCoreMissingVoiceChatScreen extends Screen {
	private static final String MODRINTH_URL = "https://cdn.modrinth.com/data/9eGKb6K1/versions/pFTZ8sqQ/voicechat-fabric-1.21.11-2.6.12.jar";

	private final Screen parent;
	private final long openedAtNanos = System.nanoTime();

	public SmpCoreMissingVoiceChatScreen(Screen parent) {
		super(Component.literal("Simple Voice Chat fehlt"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int w = Math.min(520, this.width - 40);
		int left = (this.width - w) / 2;

		int buttonW = (w - 12) / 2;
		int y = this.height - 72;

		addRenderableWidget(new SmpCoreStyledButton(left, y, buttonW, 20, Component.literal("Mods-Ordner öffnen"), this::openModsFolder));
		addRenderableWidget(new SmpCoreStyledButton(left + buttonW + 12, y, buttonW, 20, Component.literal("Download öffnen"), this::openDownload));

		addRenderableWidget(new SmpCoreStyledButton(left, this.height - 44, buttonW, 20, Component.literal("Link kopieren"), this::copyLink));
		addRenderableWidget(new SmpCoreStyledButton(left + buttonW + 12, this.height - 44, buttonW, 20, Component.literal("Schließen"), this::onClose));
	}

	@Override
	public void onClose() {
		if (minecraft != null) {
			minecraft.setScreen(parent);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		float p = introProgress();
		int a = (int) (255.0f * p);
		graphics.fillGradient(0, 0, width, height, withAlpha(0xFF140B22, a), withAlpha(0xFF0A0F25, a));

		int yOffset = Math.round((1.0f - p) * 8.0f);
		graphics.drawCenteredString(font, Component.literal("Simple Voice Chat ist nicht installiert"), width / 2, 22 + yOffset, withAlpha(0xFFFFFFFF, a));
		graphics.drawCenteredString(font, Component.literal("Für die Voice-Chat-Features brauchst du die Mod zusätzlich."), width / 2, 36 + yOffset, withAlpha(0xFFB9B9B9, a));

		int w = Math.min(520, this.width - 40);
		int left = (this.width - w) / 2;
		int textY = 64;

		Component steps = Component.literal(
				"1) Lade \"Simple Voice Chat\" (Fabric) herunter\n" +
				"2) Lege die .jar Datei in deinen mods-Ordner\n" +
				"3) Starte Minecraft neu\n\n" +
				"Tipp: In Multiplayer muss der Server Voice Chat ebenfalls installiert haben, wenn ihr miteinander reden wollt."
		);

		List<FormattedCharSequence> lines = font.split(steps, w);
		for (FormattedCharSequence line : lines) {
			graphics.drawString(font, line, left, textY, 0xEDEDED | 0xFF000000, false);
			textY += 11;
		}

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private float introProgress() {
		double elapsed = (System.nanoTime() - openedAtNanos) / 1_000_000_000.0;
		double dur = 0.22;
		float t = (float) Math.max(0.0, Math.min(1.0, elapsed / dur));
		float inv = 1.0f - t;
		return 1.0f - inv * inv * inv;
	}

	private static int withAlpha(int argb, int alpha) {
		alpha = Math.max(0, Math.min(255, alpha));
		return (alpha << 24) | (argb & 0x00FFFFFF);
	}

	private void openModsFolder() {
		try {
			Path mods = FabricLoader.getInstance().getGameDir().resolve("mods");
			Files.createDirectories(mods);
			Util.getPlatform().openPath(mods);
		} catch (Exception ignored) {
			// If the platform can't open a folder, the "copy link" button still helps.
		}
	}

	private void openDownload() {
		try {
			Util.getPlatform().openUri(URI.create(MODRINTH_URL));
		} catch (Exception ignored) {
		}
	}

	private void copyLink() {
		Minecraft client = Minecraft.getInstance();
		client.keyboardHandler.setClipboard(MODRINTH_URL);
	}
}
