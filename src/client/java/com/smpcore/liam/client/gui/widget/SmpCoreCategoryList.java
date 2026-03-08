package com.smpcore.liam.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class SmpCoreCategoryList extends ObjectSelectionList<SmpCoreCategoryList.Entry> {
	private List<Component> hoveredTooltip;

	public SmpCoreCategoryList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
		super(minecraft, width, height, top, bottom, itemHeight);
		setRenderBackground(false);
		setRenderTopAndBottom(false);
	}

	public void setLeftPos(int left) {
		super.setLeftPos(left);
	}

	public List<Component> consumeHoveredTooltip() {
		List<Component> out = hoveredTooltip;
		hoveredTooltip = null;
		return out;
	}

	@Override
	protected void renderListBackground(GuiGraphics graphics) {
		// No-op: themed background is rendered by the parent screen.
	}

	public abstract static class Entry extends ObjectSelectionList.Entry<Entry> {
		protected void setHoveredTooltip(List<Component> tooltip) {
			((SmpCoreCategoryList) this.list).hoveredTooltip = tooltip;
		}
	}

	public static final class CategoryEntry extends Entry {
		private final ItemStack icon;
		private final Component title;
		private final Component description;
		private final List<Component> tooltip;
		private final Runnable onPress;

		public CategoryEntry(ItemStack icon, Component title, Component description, List<Component> tooltip, Runnable onPress) {
			this.icon = icon;
			this.title = title;
			this.description = description;
			this.tooltip = tooltip;
			this.onPress = onPress;
		}

		@Override
		public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTick) {
			int cardX = x;
			int cardY = y + 2;
			int cardW = entryWidth;
			int cardH = entryHeight - 4;

			int bg = hovered ? 0xAA2D2B49 : 0xAA1C1830;
			int border = hovered ? 0xFF8A5CFF : 0xFF3B2B66;

			graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, bg);
			graphics.renderOutline(cardX, cardY, cardW, cardH, border);

			int iconX = cardX + 10;
			int iconY = cardY + (cardH - 16) / 2;
			graphics.renderFakeItem(icon, iconX, iconY);

			int textX = iconX + 22;
			int titleY = cardY + 6;
			int descY = titleY + 12;

			int titleColor = hovered ? 0xFFFFFF : 0xEDEDED;
			int descColor = 0xB9B9B9;

			Minecraft mc = Minecraft.getInstance();
			graphics.drawString(mc.font, title, textX, titleY, titleColor, true);

			int maxDescWidth = cardX + cardW - 10 - textX;
			List<FormattedCharSequence> lines = mc.font.split(description, Math.max(10, maxDescWidth));
			if (!lines.isEmpty()) {
				graphics.drawString(mc.font, lines.getFirst(), textX, descY, descColor, false);
			}

			if (hovered && tooltip != null && !tooltip.isEmpty()) {
				setHoveredTooltip(tooltip);
			}
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button == 0) {
				onPress.run();
				return true;
			}
			return false;
		}

		@Override
		public Component getNarration() {
			return title;
		}
	}
}

