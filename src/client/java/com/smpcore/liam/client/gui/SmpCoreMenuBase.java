package com.smpcore.liam.client.gui;

import com.smpcore.liam.config.ConfigJson;
import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.net.SmpCorePayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class SmpCoreMenuBase extends Screen {
	protected SmpCoreConfig config;
	protected final Screen parent;
	private final long openedAtNanos = System.nanoTime();

	protected SmpCoreMenuBase(Component title, Screen parent, SmpCoreConfig config) {
		super(title);
		this.parent = parent;
		this.config = config;
	}

	protected final float introProgress() {
		// Simple open animation; keep short so it doesn't feel sluggish.
		double elapsed = (System.nanoTime() - openedAtNanos) / 1_000_000_000.0;
		double dur = 0.22;
		float t = (float) Math.max(0.0, Math.min(1.0, elapsed / dur));
		// easeOutCubic
		float inv = 1.0f - t;
		return 1.0f - inv * inv * inv;
	}

	protected final void renderSmpBackground(GuiGraphics graphics) {
		float p = introProgress();
		int a = (int) (255.0f * p);
		int top = withAlpha(0xFF140B22, a);
		int bottom = withAlpha(0xFF0A0F25, a);
		graphics.fillGradient(0, 0, width, height, top, bottom);
	}

	protected final void renderSmpHeader(GuiGraphics graphics, Component title, Component subtitle) {
		float p = introProgress();
		int a = (int) (255.0f * p);
		int yOffset = Math.round((1.0f - p) * 8.0f);
		graphics.drawCenteredString(font, title, width / 2, 18 + yOffset, withAlpha(0xFFFFFFFF, a));
		if (subtitle != null && !subtitle.getString().isBlank()) {
			graphics.drawCenteredString(font, subtitle, width / 2, 30 + yOffset, withAlpha(0xFFB9B9B9, a));
		}
	}

	private static int withAlpha(int argb, int alpha) {
		alpha = Math.max(0, Math.min(255, alpha));
		return (alpha << 24) | (argb & 0x00FFFFFF);
	}

	protected void saveToServer() {
		ClientPlayNetworking.send(new SmpCorePayloads.SaveConfigPayload(ConfigJson.toJson(config)));
	}

	public void updateConfig(SmpCoreConfig newConfig) {
		this.config = newConfig;
	}

	@Override
	public void onClose() {
		if (parent != null) {
			this.minecraft.setScreen(parent);
		} else {
			super.onClose();
		}
	}
}

