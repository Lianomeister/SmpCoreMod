package com.smpcore.liam.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public final class SmpCoreCategoryList extends ObjectSelectionList<SmpCoreCategoryList.Entry> {
	private final int entryHeight;
	private List<Component> hoveredTooltip;

	public SmpCoreCategoryList(Minecraft minecraft, int width, int height, int top, int bottom, int entryHeight) {
		super(minecraft, width, height, top, bottom);
		this.entryHeight = entryHeight;
	}

	public void setLeftPos(int left) {
		setX(left);
	}

	public void addCategoryEntry(CategoryEntry entry) {
		// The vanilla method is protected; expose it for screen usage.
		addEntry(entry, entryHeight);
	}

	@Override
	protected void renderListBackground(GuiGraphics graphics) {
		// No-op: themed background is rendered by the parent screen.
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		hoveredTooltip = null;
		super.renderWidget(graphics, mouseX, mouseY, partialTick);
		Entry hovered = getHovered();
		if (hovered instanceof CategoryEntry category && category.tooltip != null && !category.tooltip.isEmpty() && isMouseOver(mouseX, mouseY)) {
			hoveredTooltip = category.tooltip;
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		// Slow down vertical scrolling a bit for better control on high-resolution wheels/touchpads.
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY * 0.65);
	}

	public List<Component> consumeHoveredTooltip() {
		List<Component> out = hoveredTooltip;
		hoveredTooltip = null;
		return out;
	}

	public abstract static class Entry extends ObjectSelectionList.Entry<Entry> {
	}

	public static final class CategoryEntry extends Entry {
		private final ItemStack icon;
		private final Component title;
		private final Component description;
		private final List<Component> tooltip;
		private final Runnable onPress;
		private final Supplier<Component> value;

		public CategoryEntry(ItemStack icon, Component title, Component description, List<Component> tooltip, Runnable onPress) {
			this(icon, title, description, tooltip, onPress, null);
		}

		public CategoryEntry(ItemStack icon, Component title, Component description, List<Component> tooltip, Runnable onPress, Supplier<Component> value) {
			this.icon = icon;
			this.title = title;
			this.description = description;
			this.tooltip = tooltip;
			this.onPress = onPress;
			this.value = value;
		}

		@Override
		public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
			int cardX = getX();
			int cardY = getY() + 2;
			int cardW = getWidth();
			int cardH = getHeight() - 4;

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
			Component valueText = value == null ? null : value.get();
			int valueX = cardX + cardW - 10;
			int valueW = 0;
			if (valueText != null) {
				// Fit the value on the right so it never overlaps the title.
				int maxValueW = Math.max(80, cardW / 3);
				String valuePlain = valueText.getString();
				String valueFitted = fitWithEllipsis(mc, valuePlain, maxValueW);
				valueW = mc.font.width(valueFitted);
				valueX = cardX + cardW - 10 - valueW;
				graphics.drawString(mc.font, valueFitted, valueX, titleY, (hovered ? 0xDCCBFF : 0xC9B6FF) | 0xFF000000, true);
			}

			int maxTitleW = valueText == null ? (cardX + cardW - 10 - textX) : Math.max(40, (valueX - 10) - textX);
			String titlePlain = title.getString();
			String titleFitted = mc.font.width(titlePlain) > maxTitleW ? fitWithEllipsis(mc, titlePlain, maxTitleW) : titlePlain;
			graphics.drawString(mc.font, titleFitted, textX, titleY, titleColor | 0xFF000000, true);

			int maxDescWidth = cardX + cardW - 10 - textX;
			List<FormattedCharSequence> lines = mc.font.split(description, Math.max(10, maxDescWidth));
			if (!lines.isEmpty()) {
				graphics.drawString(mc.font, lines.getFirst(), textX, descY, descColor | 0xFF000000, false);
			}
		}

		@Override
		public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean bl) {
			if (event.button() == 0 && isMouseOver(event.x(), event.y())) {
				onPress.run();
				return true;
			}
			return false;
		}

		@Override
		public Component getNarration() {
			return title;
		}

		private static String fitWithEllipsis(Minecraft mc, String raw, int maxWidth) {
			if (raw == null) {
				return "";
			}
			raw = raw.trim();
			if (raw.isEmpty()) {
				return "";
			}
			if (mc.font.width(raw) <= maxWidth) {
				return raw;
			}
			int ellipsisW = mc.font.width("...");
			String cut = mc.font.plainSubstrByWidth(raw, Math.max(10, maxWidth - ellipsisW)).trim();
			return cut + "...";
		}
	}
}
