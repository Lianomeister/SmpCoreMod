package com.smpcore.liam.client.gui;

import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public final class SmpCoreGameplayScreen extends SmpCoreMenuBase {
	public SmpCoreGameplayScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Gameplay"), parent, config);
	}

	@Override
	protected void init() {
		int w = 240;
		int x = (this.width - w) / 2;
		int y = 54;

		Button pvp = addRenderableWidget(toggleButton(x, y, w, "PvP", () -> config.gameplay.pvpEnabled, v -> config.gameplay.pvpEnabled = v));
		pvp.setTooltip(Tooltip.create(Component.literal("Enable/disable player vs player damage.")));
		y += 26;

		Button ax = addRenderableWidget(Button.builder(Component.literal("Anti X-Ray settings..."), b -> {
			this.minecraft.setScreen(new SmpCoreAntiXrayScreen(this, config));
		}).bounds(x, y, w, 20).build());
		ax.setTooltip(Tooltip.create(Component.literal("Configure Anti X-Ray engine mode.")));
		y += 26;
		Button spec = addRenderableWidget(toggleButton(x, y, w, "Spectator after death", () -> config.death.spectatorAfterDeath, v -> config.death.spectatorAfterDeath = v));
		spec.setTooltip(Tooltip.create(Component.literal("Hardcore-like: after death, respawn in spectator.")));

		addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose()).bounds(x, this.height - 32, 116, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Save"), b -> saveToServer()).bounds(x + 124, this.height - 32, 116, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private Button toggleButton(int x, int y, int w, String label, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
		return Button.builder(title(label, getter.getAsBoolean()), b -> {
			boolean next = !getter.getAsBoolean();
			setter.accept(next);
			b.setMessage(title(label, next));
			saveToServer();
		}).bounds(x, y, w, 20).build();
	}

	private static Component title(String label, boolean enabled) {
		return Component.literal(label + ": " + (enabled ? "Enabled" : "Disabled"));
	}
}
