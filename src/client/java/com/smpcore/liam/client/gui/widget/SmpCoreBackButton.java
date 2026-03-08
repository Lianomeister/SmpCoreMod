package com.smpcore.liam.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SmpCoreBackButton extends AbstractWidget {
	private final Runnable onPress;
	private final ItemStack icon;

	public SmpCoreBackButton(int x, int y, Runnable onPress) {
		super(x, y, 92, 20, Component.literal("Back"));
		this.onPress = onPress;
		this.icon = new ItemStack(Items.ARROW);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		boolean hovered = isHovered();
		int bg = hovered ? 0xAA2D2B49 : 0xAA1C1830;
		int border = hovered ? 0xFF8A5CFF : 0xFF3B2B66;

		graphics.fill(getX(), getY(), getX() + width, getY() + height, bg);
		graphics.renderOutline(getX(), getY(), width, height, border);

		int iconX = getX() + 6;
		int iconY = getY() + 2;
		graphics.renderFakeItem(icon, iconX, iconY);

		int textX = iconX + 20;
		int textY = getY() + 6;
		int textColor = (hovered ? 0xFFFFFF : 0xEDEDED) | 0xFF000000;
		graphics.drawString(Minecraft.getInstance().font, Component.literal("Back"), textX, textY, textColor, true);
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

