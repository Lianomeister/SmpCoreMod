package com.smpcore.liam.client.gui;

import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public final class SmpCoreEditValueScreen extends SmpCoreMenuBase {
	private final Component subtitle;
	private final List<Component> help;
	private final String initialValue;
	private final Consumer<String> onSave;
	private final boolean saveToServer;

	private EditBox value;
	private boolean saved;

	public SmpCoreEditValueScreen(SmpCoreMenuBase parent, SmpCoreConfig config, Component title, Component subtitle, String initialValue, List<Component> help, Consumer<String> onSave) {
		this(parent, config, title, subtitle, initialValue, help, onSave, true);
	}

	public SmpCoreEditValueScreen(SmpCoreMenuBase parent, SmpCoreConfig config, Component title, Component subtitle, String initialValue, List<Component> help, Consumer<String> onSave, boolean saveToServer) {
		super(title, parent, config);
		this.subtitle = subtitle;
		this.initialValue = initialValue;
		this.help = help == null ? List.of() : help;
		this.onSave = onSave;
		this.saveToServer = saveToServer;
	}

	@Override
	protected void init() {
		int w = Math.min(320, this.width - 40);
		int x = (this.width - w) / 2;

		value = addRenderableWidget(new EditBox(font, x, 64, w, 20, Component.literal("Value")));
		value.setValue(initialValue == null ? "" : initialValue);
		if (!help.isEmpty()) {
			value.setTooltip(Tooltip.create(help.getFirst()));
		}

		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(x, this.height - 32, w, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		if (subtitle != null) {
			graphics.drawCenteredString(font, subtitle, width / 2, 30, 0xB9B9B9);
		}

		int y = 92;
		for (Component line : help) {
			graphics.drawCenteredString(font, line, width / 2, y, 0x9A9A9A);
			y += 10;
		}

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private void save() {
		if (saved) {
			return;
		}
		saved = true;
		onSave.accept(value.getValue());
		if (saveToServer) {
			saveToServer();
		}
	}

	@Override
	public void onClose() {
		save();
		super.onClose();
	}
}
