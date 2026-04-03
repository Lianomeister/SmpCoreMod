package com.smpcore.liam.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public final class SmpCoreStyledButton extends AbstractWidget {
	private final Runnable onPress;
	private final ItemStack icon;
	private long lastNanos;
	private float hoverT;
	private float clickT;

	public SmpCoreStyledButton(int x, int y, int width, int height, Component label, Runnable onPress) {
		this(x, y, width, height, label, null, onPress);
	}

	public SmpCoreStyledButton(int x, int y, int width, int height, Component label, ItemStack icon, Runnable onPress) {
		super(x, y, width, height, label);
		this.onPress = onPress;
		this.icon = icon;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		long now = System.nanoTime();
		float dt = lastNanos == 0 ? 0.0f : (now - lastNanos) / 1_000_000_000.0f;
		lastNanos = now;
		dt = Math.min(dt, 0.05f);

		hoverT = expApproach(hoverT, isHovered() ? 1.0f : 0.0f, 14.0f, dt);
		clickT = expApproach(clickT, 0.0f, 18.0f, dt);

		float scale = 1.0f + 0.02f * hoverT - 0.03f * clickT;

		int bg = lerpArgb(0xAA1C1830, 0xAA2D2B49, hoverT);
		int border = lerpArgb(0xFF3B2B66, 0xFF8A5CFF, hoverT);

		Matrix3x2fStack pose = graphics.pose();
		float cx = getX() + width / 2.0f;
		float cy = getY() + height / 2.0f;

		pose.pushMatrix();
		pose.translate(cx, cy);
		pose.scale(scale, scale);
		pose.translate(-cx, -cy);

		graphics.fill(getX(), getY(), getX() + width, getY() + height, bg);
		graphics.renderOutline(getX(), getY(), width, height, border);

		int leftPad = 10;
		if (icon != null) {
			int iconX = getX() + 10;
			int iconY = getY() + (height - 16) / 2;
			graphics.renderFakeItem(icon, iconX, iconY);
			leftPad = 32;
		}

		var font = Minecraft.getInstance().font;
		Component msg = getMessage();
		int textColor = lerpArgb(0xFFEDEDED, 0xFFFFFFFF, hoverT);

		int textY = getY() + (height - 8) / 2;
		if (icon == null) {
			graphics.drawCenteredString(font, msg, getX() + width / 2, textY, textColor);
		} else {
			graphics.drawString(font, msg, getX() + leftPad, textY, textColor, true);
		}

		pose.popMatrix();
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean bl) {
		if (!this.active) {
			return;
		}
		if (event.button() == 0) {
			clickT = 1.0f;
			onPress.run();
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}

	private static float expApproach(float current, float target, float speed, float dt) {
		if (dt <= 0.0f) {
			return current;
		}
		float k = (float) (1.0 - Math.exp(-speed * dt));
		return current + (target - current) * k;
	}

	private static int lerpArgb(int a, int b, float t) {
		t = Math.max(0.0f, Math.min(1.0f, t));
		int aA = (a >>> 24) & 0xFF;
		int aR = (a >>> 16) & 0xFF;
		int aG = (a >>> 8) & 0xFF;
		int aB = a & 0xFF;
		int bA = (b >>> 24) & 0xFF;
		int bR = (b >>> 16) & 0xFF;
		int bG = (b >>> 8) & 0xFF;
		int bB = b & 0xFF;
		int oA = aA + Math.round((bA - aA) * t);
		int oR = aR + Math.round((bR - aR) * t);
		int oG = aG + Math.round((bG - aG) * t);
		int oB = aB + Math.round((bB - aB) * t);
		return (oA << 24) | (oR << 16) | (oG << 8) | oB;
	}
}

