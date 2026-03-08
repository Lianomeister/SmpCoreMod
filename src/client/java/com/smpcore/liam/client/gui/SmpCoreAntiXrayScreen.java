package com.smpcore.liam.client.gui;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public final class SmpCoreAntiXrayScreen extends SmpCoreMenuBase {
	private Button enabledButton;
	private Button modeButton;

	public SmpCoreAntiXrayScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Anti X-Ray"), parent, config);
	}

	@Override
	protected void init() {
		int w = 260;
		int x = (this.width - w) / 2;
		int y = 54;

		enabledButton = addRenderableWidget(Button.builder(enabledTitle(), b -> {
			config.gameplay.antiXrayEnabled = !config.gameplay.antiXrayEnabled;
			b.setMessage(enabledTitle());
			saveToServer();
		}).bounds(x, y, w, 20).build());
		enabledButton.setTooltip(Tooltip.create(Component.literal("Master toggle for Anti X-Ray engine.")));
		y += 26;

		modeButton = addRenderableWidget(Button.builder(modeTitle(), b -> {
			config.gameplay.antiXrayMode = next(config.gameplay.antiXrayMode);
			b.setMessage(modeTitle());
			saveToServer();
		}).bounds(x, y, w, 20).build());
		modeButton.setTooltip(Tooltip.create(Component.literal("Switch between engine modes (implementation WIP).")));

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.literal("Choose how the server hides ore information"), width / 2, 30, 0xB9B9B9);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private Component enabledTitle() {
		return Component.literal("Enabled: " + (config.gameplay.antiXrayEnabled ? "Yes" : "No"));
	}

	private Component modeTitle() {
		return Component.literal("Engine mode: " + config.gameplay.antiXrayMode.name());
	}

	private static SmpCoreConfig.AntiXrayMode next(SmpCoreConfig.AntiXrayMode mode) {
		SmpCoreConfig.AntiXrayMode[] values = SmpCoreConfig.AntiXrayMode.values();
		return values[(mode.ordinal() + 1) % values.length];
	}
}
