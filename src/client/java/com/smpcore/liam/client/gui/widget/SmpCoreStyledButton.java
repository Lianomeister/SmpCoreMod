package com.smpcore.liam.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class SmpCoreStyledButton extends AbstractWidget {
	private final Runnable onPress;
	private final ItemStack icon;

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
		boolean hovered = isHovered();
		int bg = hovered ? 0xAA2D2B49 : 0xAA1C1830;
		int border = hovered ? 0xFF8A5CFF : 0xFF3B2B66;

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
		int textColor = (hovered ? 0xFFFFFF : 0xEDEDED) | 0xFF000000;

		int textY = getY() + (height - 8) / 2;
		if (icon == null) {
			graphics.drawCenteredString(font, msg, getX() + width / 2, textY, textColor);
		} else {
			graphics.drawString(font, msg, getX() + leftPad, textY, textColor, true);
		}
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean bl) {
		if (!this.active) {
			return;
		}
		if (event.button() == 0) {
			onPress.run();
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}

