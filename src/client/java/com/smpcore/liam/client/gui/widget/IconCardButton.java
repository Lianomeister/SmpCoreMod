package com.smpcore.liam.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class IconCardButton extends AbstractWidget {
	private final ItemStack icon;
	private final Runnable onPress;

	public IconCardButton(int x, int y, int width, int height, ItemStack icon, Component message, Runnable onPress) {
		super(x, y, width, height, message);
		this.icon = icon;
		this.onPress = onPress;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		boolean hovered = isHovered();
		int bg = hovered ? 0xAA2D2B49 : 0xAA1C1830;
		int border = hovered ? 0xFF8A5CFF : 0xFF3B2B66;

		graphics.fill(getX(), getY(), getX() + width, getY() + height, bg);
		graphics.renderOutline(getX(), getY(), width, height, border);

		int iconSize = 16;
		int iconX = getX() + 10;
		int iconY = getY() + (height - iconSize) / 2;
		graphics.renderFakeItem(icon, iconX, iconY);

		int textX = iconX + 22;
		int textY = getY() + (height - 8) / 2;
		int textColor = hovered ? 0xFFFFFF : 0xE6E6E6;
		graphics.drawString(Minecraft.getInstance().font, getMessage(), textX, textY, textColor, true);
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean bl) {
		if (!this.active) {
			return;
		}
		this.onPress.run();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
	}
}
