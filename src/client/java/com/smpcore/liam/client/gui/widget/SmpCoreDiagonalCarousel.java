package com.smpcore.liam.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public final class SmpCoreDiagonalCarousel extends AbstractWidget {
	public record Entry(
			ItemStack icon,
			Component title,
			Component description,
			List<Component> tooltip,
			Runnable onPress,
			Supplier<Component> value
	) {
	}

	private final List<Entry> entries;
	private double scrollX;
	private double targetScrollX;
	private List<Component> hoveredTooltip;
	private long lastNanos;

	public SmpCoreDiagonalCarousel(int x, int y, int width, int height, List<Entry> entries) {
		super(x, y, width, height, Component.empty());
		this.entries = entries;
	}

	public List<Component> consumeHoveredTooltip() {
		List<Component> out = hoveredTooltip;
		hoveredTooltip = null;
		return out;
	}

	private record Card(int index, Entry entry, int x, int y, int drawY, float depth, float scale, int scaledLeft, int scaledTop, int scaledRight, int scaledBottom) {
	}

	private List<Card> layoutCards() {
		int cardW = Math.min(240, this.width - 24);
		int cardH = 84;
		int gapX = 12;

		int baseX = getX() + 8;
		int baseY = getY() + (this.height - cardH) / 2;
		baseY = clampInt(baseY, getY() + 10, getY() + this.height - cardH - 10);

		int maxX = baseX + (entries.size() - 1) * (cardW + gapX);
		int minScroll = 0;
		int maxScroll = Math.max(0, (maxX + cardW) - (getX() + this.width - 6));
		targetScrollX = clamp(targetScrollX, minScroll, maxScroll);
		scrollX = clamp(scrollX, minScroll, maxScroll);

		int centerX = getX() + this.width / 2;
		double focusRange = Math.max(80.0, this.width * 0.55);
		int slopeAmplitude = Math.min(28, Math.max(8, (this.height - cardH - 20) / 3));

		float minScale = 0.90f;
		float maxScale = 1.10f;
		float maxLiftPx = 12.0f;

		ArrayList<Card> out = new ArrayList<>(entries.size());
		for (int i = 0; i < entries.size(); i++) {
			Entry e = entries.get(i);

			int x = (int) Math.round(baseX + i * (cardW + gapX) - scrollX);
			double dx = (x + cardW / 2.0) - centerX;
			double dxNorm = clamp(dx / focusRange, -1.0, 1.0);

			// Diagonal positioning: clamp by focusRange so far-away entries don't drift down when scrolling.
			int y = baseY + (int) Math.round(-dxNorm * slopeAmplitude);

			float depth = (float) (1.0 - clamp(Math.abs(dx) / focusRange, 0.0, 1.0));
			float scale = lerp(minScale, maxScale, depth);
			int lift = Math.round(depth * maxLiftPx);
			int drawY = y - lift;

			float pivotX = x + cardW / 2.0f;
			float pivotY = drawY + cardH / 2.0f;
			int scaledW = Math.round(cardW * scale);
			int scaledH = Math.round(cardH * scale);
			int left = Math.round(pivotX - scaledW / 2.0f);
			int top = Math.round(pivotY - scaledH / 2.0f);
			int right = left + scaledW;
			int bottom = top + scaledH;

			out.add(new Card(i, e, x, y, drawY, depth, scale, left, top, right, bottom));
		}
		return out;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		hoveredTooltip = null;

		long now = System.nanoTime();
		float dt = lastNanos == 0 ? 0.0f : (now - lastNanos) / 1_000_000_000.0f;
		lastNanos = now;
		dt = Math.min(dt, 0.05f);
		scrollX = expApproach(scrollX, targetScrollX, 16.0, dt);

		List<Card> cards = layoutCards();
		// Draw far -> near so the centered card can overlap others (3D/coverflow feel).
		cards.sort(Comparator.comparingDouble(c -> c.depth()));

		int cardW = Math.min(240, this.width - 24);
		int cardH = 84;

		Minecraft mc = Minecraft.getInstance();
		Matrix3x2fStack pose = graphics.pose();

		for (Card card : cards) {
			int x = card.x();
			int y = card.drawY();

			// quick cull (use scaled bounds)
			if (card.scaledRight() < getX() || card.scaledLeft() > getX() + this.width || card.scaledBottom() < getY() || card.scaledTop() > getY() + this.height) {
				continue;
			}

			boolean hovered = mouseX >= card.scaledLeft() && mouseY >= card.scaledTop() && mouseX < card.scaledRight() && mouseY < card.scaledBottom();

			float depth = card.depth();
			int bg = hovered ? 0xAA2D2B49 : 0xAA1C1830;
			int border = hovered ? 0xFF8A5CFF : 0xFF3B2B66;

			// Slightly brighten the centered card.
			int extra = Math.round(depth * 28.0f);
			bg = withRgbAdd(bg, extra, extra, extra);
			border = withRgbAdd(border, extra, extra, extra);

			float pivotX = x + cardW / 2.0f;
			float pivotY = y + cardH / 2.0f;

			// Drop shadow (screen space, not scaled) for extra depth.
			int shadowOffset = Math.round(2.0f + 3.0f * depth);
			int shadowAlpha = Math.round(28.0f + 70.0f * depth);
			int shadow = (shadowAlpha << 24);
			graphics.fill(x + shadowOffset, y + shadowOffset, x + cardW + shadowOffset, y + cardH + shadowOffset, shadow);

			pose.pushMatrix();
			pose.translate(pivotX, pivotY);
			pose.scale(card.scale(), card.scale());
			pose.translate(-pivotX, -pivotY);

			graphics.fill(x, y, x + cardW, y + cardH, bg);
			graphics.renderOutline(x, y, cardW, cardH, border);

			int iconX = x + 10;
			int iconY = y + (cardH - 16) / 2;
			graphics.renderFakeItem(card.entry().icon(), iconX, iconY);

			int textX = iconX + 22;
			int titleY = y + 10;
			int descY = titleY + 14;

			Component valueText = card.entry().value() == null ? null : card.entry().value().get();
			if (valueText != null) {
				int valueW = mc.font.width(valueText);
				int valueX = x + cardW - 10 - valueW;
				graphics.drawString(mc.font, valueText, valueX, titleY, (hovered ? 0xDCCBFF : 0xC9B6FF) | 0xFF000000, true);
			}

			graphics.drawString(mc.font, card.entry().title(), textX, titleY, (hovered ? 0xFFFFFF : 0xEDEDED) | 0xFF000000, true);

			int maxDescWidth = x + cardW - 10 - textX;
			List<FormattedCharSequence> lines = mc.font.split(card.entry().description(), Math.max(10, maxDescWidth));
			if (!lines.isEmpty()) {
				graphics.drawString(mc.font, lines.getFirst(), textX, descY, 0xB9B9B9 | 0xFF000000, false);
			}

			pose.popMatrix();

			if (hovered && card.entry().tooltip() != null && !card.entry().tooltip().isEmpty()) {
				hoveredTooltip = card.entry().tooltip();
			}
		}
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean bl) {
		if (!this.active) {
			return;
		}

		double mx = event.x();
		double my = event.y();

		// Prefer the top-most card when overlaps happen.
		List<Card> cards = layoutCards();
		cards.sort(Comparator.comparingDouble((Card c) -> c.depth()).reversed());

		for (Card card : cards) {
			if (mx >= card.scaledLeft() && my >= card.scaledTop() && mx < card.scaledRight() && my < card.scaledBottom()) {
				if (event.button() == 0) {
					card.entry().onPress().run();
				}
				return;
			}
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return false;
		}
		// Use vertical wheel to scroll horizontally.
		this.targetScrollX = this.targetScrollX - scrollY * 34.0;
		return true;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}

	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}

	private static int clampInt(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}

	private static double expApproach(double current, double target, double speed, float dt) {
		if (dt <= 0.0f) {
			return current;
		}
		double k = 1.0 - Math.exp(-speed * dt);
		return current + (target - current) * k;
	}

	private static int withRgbAdd(int argb, int addR, int addG, int addB) {
		int a = (argb >>> 24) & 0xFF;
		int r = (argb >>> 16) & 0xFF;
		int g = (argb >>> 8) & 0xFF;
		int b = (argb) & 0xFF;
		r = clampInt(r + addR, 0, 255);
		g = clampInt(g + addG, 0, 255);
		b = clampInt(b + addB, 0, 255);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
}
