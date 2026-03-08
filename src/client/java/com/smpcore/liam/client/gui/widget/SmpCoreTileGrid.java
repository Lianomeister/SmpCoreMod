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

public final class SmpCoreTileGrid extends AbstractWidget {
	public record Entry(
			ItemStack icon,
			Component title,
			Component description,
			List<Component> tooltip,
			Runnable onPress,
			Supplier<Component> value
	) {
	}

	private record Tile(
			int index,
			Entry entry,
			int x,
			int y,
			int size,
			int drawY,
			float depth,
			float scale,
			int scaledLeft,
			int scaledTop,
			int scaledRight,
			int scaledBottom
	) {
	}

	private final List<Entry> entries;
	private List<Component> hoveredTooltip;

	public SmpCoreTileGrid(int x, int y, int width, int height, List<Entry> entries) {
		super(x, y, width, height, Component.empty());
		this.entries = entries;
	}

	public List<Component> consumeHoveredTooltip() {
		List<Component> out = hoveredTooltip;
		hoveredTooltip = null;
		return out;
	}

	private List<Tile> layoutTiles() {
		int padding = 12;
		int gap = 10;

		int cols = 5;
		int rows = 3;

		// If the grid is too narrow, reduce columns to keep tiles square.
		int innerW = Math.max(0, this.width - padding * 2);
		int innerH = Math.max(0, this.height - padding * 2);
		int desiredMinTile = 78;
		while (cols > 2) {
			int tile = (innerW - gap * (cols - 1)) / cols;
			if (tile >= desiredMinTile) {
				break;
			}
			cols--;
		}

		int maxVisible = cols * rows;
		int tileSizeByW = (innerW - gap * (cols - 1)) / cols;
		int tileSizeByH = (innerH - gap * (rows - 1)) / rows;
		// Never exceed available space; if the window is small, tiles will shrink instead of overflowing.
		int tileSize = Math.min(tileSizeByW, tileSizeByH);

		// Center the grid within our widget bounds.
		int gridW = cols * tileSize + gap * (cols - 1);
		int gridH = rows * tileSize + gap * (rows - 1);
		int startX = getX() + (this.width - gridW) / 2;
		int startY = getY() + (this.height - gridH) / 2;

		float minScale = 0.94f;
		float maxScale = 1.08f;
		float maxLiftPx = 8.0f;
		int centerX = getX() + this.width / 2;
		int centerY = getY() + this.height / 2;
		double focusRange = Math.max(90.0, Math.min(this.width, this.height) * 0.65);

		ArrayList<Tile> out = new ArrayList<>(Math.min(entries.size(), maxVisible));
		for (int i = 0; i < entries.size(); i++) {
			int slot = i;
			if (slot >= maxVisible) {
				break;
			}

			int col = slot % cols;
			int row = slot / cols;
			int x = startX + col * (tileSize + gap);
			int y = startY + row * (tileSize + gap);

			double dx = (x + tileSize / 2.0) - centerX;
			double dy = (y + tileSize / 2.0) - centerY;
			double dist = Math.sqrt(dx * dx + dy * dy);

			float depth = (float) (1.0 - clamp(dist / focusRange, 0.0, 1.0));
			float scale = lerp(minScale, maxScale, depth);
			int lift = Math.round(depth * maxLiftPx);
			int drawY = y - lift;

			float pivotX = x + tileSize / 2.0f;
			float pivotY = drawY + tileSize / 2.0f;
			int scaled = Math.round(tileSize * scale);
			int left = Math.round(pivotX - scaled / 2.0f);
			int top = Math.round(pivotY - scaled / 2.0f);

			out.add(new Tile(i, entries.get(i), x, y, tileSize, drawY, depth, scale, left, top, left + scaled, top + scaled));
		}

		return out;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		hoveredTooltip = null;

		List<Tile> tiles = layoutTiles();
		tiles.sort(Comparator.comparingDouble(t -> t.depth()));

		Minecraft mc = Minecraft.getInstance();
		Matrix3x2fStack pose = graphics.pose();

		Tile hoveredTile = null;
		for (Tile t : tiles) {
			if (mouseX >= t.scaledLeft() && mouseY >= t.scaledTop() && mouseX < t.scaledRight() && mouseY < t.scaledBottom()) {
				hoveredTile = t;
				break;
			}
		}

		for (Tile tile : tiles) {
			int x = tile.x();
			int y = tile.drawY();
			int s = tile.size();

			boolean hovered = hoveredTile != null && hoveredTile.index() == tile.index();

			float extraScale = hovered ? 0.04f : 0.0f;
			float scale = tile.scale() + extraScale;

			float depth = tile.depth();
			int bg = hovered ? 0xAA2D2B49 : 0xAA1C1830;
			int border = hovered ? 0xFF8A5CFF : 0xFF3B2B66;

			int extra = Math.round(depth * 24.0f);
			bg = withRgbAdd(bg, extra, extra, extra);
			border = withRgbAdd(border, extra, extra, extra);

			int shadowOffset = Math.round(2.0f + 2.0f * depth);
			int shadowAlpha = Math.round(18.0f + 55.0f * depth);
			int shadow = (shadowAlpha << 24);
			graphics.fill(x + shadowOffset, y + shadowOffset, x + s + shadowOffset, y + s + shadowOffset, shadow);

			float pivotX = x + s / 2.0f;
			float pivotY = y + s / 2.0f;

			pose.pushMatrix();
			pose.translate(pivotX, pivotY);
			pose.scale(scale, scale);
			pose.translate(-pivotX, -pivotY);

			graphics.fill(x, y, x + s, y + s, bg);
			graphics.renderOutline(x, y, s, s, border);

			// Icon (centered, top)
			int iconX = x + (s - 16) / 2;
			int iconY = y + 16;
			graphics.renderFakeItem(tile.entry().icon(), iconX, iconY);

			// Title (centered, bottom; avoid ugly single-letter wrapping like "Cooldown" + "s")
			int textW = s - 12;
			String titlePlain = tile.entry().title().getString();
			int lineH = 9;
			int textColor = (hovered ? 0xFFFFFF : 0xEDEDED) | 0xFF000000;
			if (!titlePlain.contains(" ")) {
				String fitted = titlePlain;
				if (mc.font.width(fitted) > textW) {
					int ellipsisW = mc.font.width("...");
					fitted = mc.font.plainSubstrByWidth(fitted, Math.max(10, textW - ellipsisW)).trim() + "...";
				}
				int lw = mc.font.width(fitted);
				int textX = x + (s - lw) / 2;
				int textY = y + s - 16 - lineH;
				graphics.drawString(mc.font, fitted, textX, textY, textColor, true);
			} else {
				List<FormattedCharSequence> lines = mc.font.split(tile.entry().title(), Math.max(20, textW));
				int maxLines = Math.min(2, lines.size());
				int textY = y + s - 14 - (maxLines * lineH);
				for (int li = 0; li < maxLines; li++) {
					FormattedCharSequence line = lines.get(li);
					int lw = mc.font.width(line);
					int textX = x + (s - lw) / 2;
					graphics.drawString(mc.font, line, textX, textY + li * lineH, textColor, true);
				}
			}

			pose.popMatrix();

			if (hovered && tile.entry().tooltip() != null && !tile.entry().tooltip().isEmpty()) {
				hoveredTooltip = tile.entry().tooltip();
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

		List<Tile> tiles = layoutTiles();
		tiles.sort(Comparator.comparingDouble((Tile t) -> t.depth()).reversed());

		for (Tile tile : tiles) {
			if (mx >= tile.scaledLeft() && my >= tile.scaledTop() && mx < tile.scaledRight() && my < tile.scaledBottom()) {
				if (event.button() == 0) {
					tile.entry().onPress().run();
				}
				return;
			}
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}

	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}

	private static int clampInt(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
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
