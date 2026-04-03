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
	private long lastNanos;
	private float[] hoverByIndex = new float[0];
	private int page;

	public SmpCoreTileGrid(int x, int y, int width, int height, List<Entry> entries) {
		super(x, y, width, height, Component.empty());
		this.entries = entries;
	}

	public List<Component> consumeHoveredTooltip() {
		List<Component> out = hoveredTooltip;
		hoveredTooltip = null;
		return out;
	}

	public int page() {
		return page;
	}

	public int pageCount() {
		int pageSize = Math.max(1, maxVisibleTiles());
		return Math.max(1, (entries.size() + pageSize - 1) / pageSize);
	}

	public void nextPage() {
		setPage(page + 1);
	}

	public void prevPage() {
		setPage(page - 1);
	}

	public void setPage(int page) {
		int clamped = clampInt(page, 0, pageCount() - 1);
		if (this.page != clamped) {
			this.page = clamped;
			hoverByIndex = new float[0];
		}
	}

	private int maxVisibleTiles() {
		Layout l = computeLayout();
		return l.cols * l.rows;
	}

	private record Layout(int cols, int rows, int tileSize, int startX, int startY, int gap, int padding) {
	}

	private Layout computeLayout() {
		int padding = 12;
		int gap = 12;

		// Bigger tiles: start with fewer rows/cols.
		int cols = 3;
		int rows = 2;

		int innerW = Math.max(0, this.width - padding * 2);
		int innerH = Math.max(0, this.height - padding * 2);

		int tileSizeByW = (innerW - gap * (cols - 1)) / cols;
		int tileSizeByH = (innerH - gap * (rows - 1)) / rows;
		int tileSize = Math.min(tileSizeByW, tileSizeByH);

		// If the screen is very small, reduce columns first.
		int desiredMinTile = 150;
		while (cols > 2 && tileSize < desiredMinTile) {
			cols--;
			tileSizeByW = (innerW - gap * (cols - 1)) / cols;
			tileSize = Math.min(tileSizeByW, tileSizeByH);
		}

		// If there's tons of room, allow 3 rows for more visible tiles without shrinking too much.
		if (rows < 3) {
			int tileSizeByH3 = (innerH - gap * (3 - 1)) / 3;
			int tileSizeCandidate = Math.min(tileSizeByW, tileSizeByH3);
			if (tileSizeCandidate >= desiredMinTile) {
				rows = 3;
				tileSize = tileSizeCandidate;
			}
		}

		int gridW = cols * tileSize + gap * (cols - 1);
		int gridH = rows * tileSize + gap * (rows - 1);
		int startX = getX() + (this.width - gridW) / 2;
		int startY = getY() + (this.height - gridH) / 2;
		return new Layout(cols, rows, tileSize, startX, startY, gap, padding);
	}

	private List<Tile> layoutTiles() {
		Layout layout = computeLayout();
		int cols = layout.cols();
		int rows = layout.rows();
		int tileSize = layout.tileSize();
		int startX = layout.startX();
		int startY = layout.startY();
		int gap = layout.gap();

		int maxVisible = cols * rows;
		int pageSize = Math.max(1, maxVisible);
		int maxPage = Math.max(0, (entries.size() + pageSize - 1) / pageSize - 1);
		page = clampInt(page, 0, maxPage);
		int baseIndex = page * pageSize;

		float minScale = 0.94f;
		float maxScale = 1.08f;
		float maxLiftPx = 8.0f;
		int centerX = getX() + this.width / 2;
		int centerY = getY() + this.height / 2;
		double focusRange = Math.max(90.0, Math.min(this.width, this.height) * 0.65);

		int visibleCount = Math.min(maxVisible, Math.max(0, entries.size() - baseIndex));
		ArrayList<Tile> out = new ArrayList<>(visibleCount);
		for (int slot = 0; slot < visibleCount; slot++) {
			int entryIndex = baseIndex + slot;
			if (entryIndex >= entries.size()) {
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

			out.add(new Tile(entryIndex, entries.get(entryIndex), x, y, tileSize, drawY, depth, scale, left, top, left + scaled, top + scaled));
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

		if (hoverByIndex.length != entries.size()) {
			hoverByIndex = new float[entries.size()];
		}

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

		int hoveredIndex = hoveredTile == null ? -1 : hoveredTile.index();
		for (int i = 0; i < hoverByIndex.length; i++) {
			float target = i == hoveredIndex ? 1.0f : 0.0f;
			hoverByIndex[i] = expApproach(hoverByIndex[i], target, 16.0f, dt);
		}

		for (Tile tile : tiles) {
			int x = tile.x();
			float hoverT = hoverByIndex[tile.index()];
			int y = tile.drawY() - Math.round(hoverT * 4.0f);
			int s = tile.size();

			boolean hovered = hoveredTile != null && hoveredTile.index() == tile.index();

			float extraScale = 0.05f * hoverT;
			float scale = tile.scale() + extraScale;

			float depth = tile.depth();
			int bg = lerpArgb(0xAA1C1830, 0xAA2D2B49, hoverT);
			int border = lerpArgb(0xFF3B2B66, 0xFF8A5CFF, hoverT);

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
			int iconY = y + 12;
			graphics.renderFakeItem(tile.entry().icon(), iconX, iconY);

			// Title (centered, bottom; avoid ugly single-letter wrapping like "Cooldown" + "s")
			int textW = s - 12;
			String titlePlain = tile.entry().title().getString();
			int lineH = 9;
			int textColor = (hovered ? 0xFFFFFF : 0xEDEDED) | 0xFF000000;
			int bottomPad = 10;
			if (!titlePlain.contains(" ")) {
				String fitted = titlePlain;
				if (mc.font.width(fitted) > textW) {
					int ellipsisW = mc.font.width("...");
					fitted = mc.font.plainSubstrByWidth(fitted, Math.max(10, textW - ellipsisW)).trim() + "...";
				}
				int lw = mc.font.width(fitted);
				int textX = x + (s - lw) / 2;
				int textY = y + s - bottomPad - lineH;
				graphics.drawString(mc.font, fitted, textX, textY, textColor, true);
			} else {
				List<FormattedCharSequence> lines = mc.font.split(tile.entry().title(), Math.max(20, textW));
				int maxLines = Math.min(2, lines.size());
				int textY = y + s - bottomPad - (maxLines * lineH);
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

		// Page indicator
		int pageCount = pageCount();
		if (pageCount > 1) {
			String label = (page + 1) + "/" + pageCount;
			int labelW = mc.font.width(label);
			int px = getX() + (this.width - labelW) / 2;
			int py = getY() + this.height - 10;
			graphics.drawString(mc.font, label, px, py, 0xB9B9B9 | 0xFF000000, true);
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
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return false;
		}
		if (scrollY > 0) {
			prevPage();
		} else if (scrollY < 0) {
			nextPage();
		}
		return true;
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
