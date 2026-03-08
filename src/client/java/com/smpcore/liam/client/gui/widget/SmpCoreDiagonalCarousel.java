package com.smpcore.liam.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

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
	private List<Component> hoveredTooltip;

	public SmpCoreDiagonalCarousel(int x, int y, int width, int height, List<Entry> entries) {
		super(x, y, width, height, Component.empty());
		this.entries = entries;
	}

	public List<Component> consumeHoveredTooltip() {
		List<Component> out = hoveredTooltip;
		hoveredTooltip = null;
		return out;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		hoveredTooltip = null;

		int n = entries.size();

		// Make the first view feel more "tile-like" (less long rectangles) and pack cards closer.
		int cardW = Math.min(220, this.width - 24);
		int cardH = 64;
		int gapX = 18;

		// Diagonal effect should depend on current on-screen X (not on index),
		// so items don't drift further down when you scroll to later entries.
		int slopeAmplitude = Math.min(28, Math.max(10, (this.height - cardH - 16) / 3));
		double slopePerPx = this.width <= 0 ? 0.0 : (double) slopeAmplitude / (double) this.width;

		int baseX = getX() + 10;
		int baseY = getY() + (this.height - cardH) / 2;
		baseY = clampInt(baseY, getY() + 8, getY() + this.height - cardH - 8);

		int maxX = baseX + (entries.size() - 1) * (cardW + gapX);
		int minScroll = 0;
		int maxScroll = Math.max(0, (maxX + cardW) - (getX() + this.width - 6));
		scrollX = clamp(scrollX, minScroll, maxScroll);

		for (int i = 0; i < entries.size(); i++) {
			Entry e = entries.get(i);

			int x = (int) Math.round(baseX + i * (cardW + gapX) - scrollX);
			int centerX = getX() + this.width / 2;
			int y = baseY + (int) Math.round((x - centerX) * slopePerPx);
			y = clampInt(y, getY() + 8, getY() + this.height - cardH - 8);

			// quick cull
			if (x + cardW < getX() || x > getX() + this.width || y + cardH < getY() || y > getY() + this.height) {
				continue;
			}

			boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + cardW && mouseY < y + cardH;

			int bg = hovered ? 0xAA2D2B49 : 0xAA1C1830;
			int border = hovered ? 0xFF8A5CFF : 0xFF3B2B66;

			graphics.fill(x, y, x + cardW, y + cardH, bg);
			graphics.renderOutline(x, y, cardW, cardH, border);

			int iconX = x + 10;
			int iconY = y + (cardH - 16) / 2;
			graphics.renderFakeItem(e.icon(), iconX, iconY);

			int textX = iconX + 22;
			int titleY = y + 10;
			int descY = titleY + 14;

			Minecraft mc = Minecraft.getInstance();
			Component valueText = e.value() == null ? null : e.value().get();
			if (valueText != null) {
				int valueW = mc.font.width(valueText);
				int valueX = x + cardW - 10 - valueW;
				graphics.drawString(mc.font, valueText, valueX, titleY, (hovered ? 0xDCCBFF : 0xC9B6FF) | 0xFF000000, true);
			}

			graphics.drawString(mc.font, e.title(), textX, titleY, (hovered ? 0xFFFFFF : 0xEDEDED) | 0xFF000000, true);

			int maxDescWidth = x + cardW - 10 - textX;
			List<FormattedCharSequence> lines = mc.font.split(e.description(), Math.max(10, maxDescWidth));
			if (!lines.isEmpty()) {
				graphics.drawString(mc.font, lines.getFirst(), textX, descY, 0xB9B9B9 | 0xFF000000, false);
			}

			if (hovered && e.tooltip() != null && !e.tooltip().isEmpty()) {
				hoveredTooltip = e.tooltip();
			}
		}
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean bl) {
		if (!this.active) {
			return;
		}

		int n = entries.size();

		int cardW = Math.min(220, this.width - 24);
		int cardH = 64;
		int gapX = 18;

		int slopeAmplitude = Math.min(28, Math.max(10, (this.height - cardH - 16) / 3));
		double slopePerPx = this.width <= 0 ? 0.0 : (double) slopeAmplitude / (double) this.width;

		int baseX = getX() + 10;
		int baseY = getY() + (this.height - cardH) / 2;
		baseY = clampInt(baseY, getY() + 8, getY() + this.height - cardH - 8);

		double mx = event.x();
		double my = event.y();

		for (int i = 0; i < entries.size(); i++) {
			int x = (int) Math.round(baseX + i * (cardW + gapX) - scrollX);
			int centerX = getX() + this.width / 2;
			int y = baseY + (int) Math.round((x - centerX) * slopePerPx);
			y = clampInt(y, getY() + 8, getY() + this.height - cardH - 8);
			if (mx >= x && my >= y && mx < x + cardW && my < y + cardH) {
				if (event.button() == 0) {
					entries.get(i).onPress().run();
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
		this.scrollX = this.scrollX - scrollY * 34.0;
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
}
