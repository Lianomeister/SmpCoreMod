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
	private static final String MODRINTH_URL = "https://modrinth.com/mod/simple-voice-chat";

	private final Screen parent;

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
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);

		graphics.drawCenteredString(font, Component.literal("Simple Voice Chat ist nicht installiert"), width / 2, 22, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.literal("Für die Voice-Chat-Features brauchst du die Mod zusätzlich."), width / 2, 36, 0xB9B9B9);

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
			graphics.drawString(font, line, left, textY, 0xEDEDED, false);
			textY += 11;
		}

		super.render(graphics, mouseX, mouseY, partialTick);
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
