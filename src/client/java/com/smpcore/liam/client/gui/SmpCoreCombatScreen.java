package com.smpcore.liam.client.gui;

import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public final class SmpCoreCombatScreen extends SmpCoreMenuBase {
	private EditBox tagSeconds;
	private EditBox damageMultiplier;

	public SmpCoreCombatScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Combat"), parent, config);
	}

	@Override
	protected void init() {
		int w = 260;
		int x = (this.width - w) / 2;
		int y = 44;

		Button enabled = addRenderableWidget(Button.builder(enabledTitle(), b -> {
			config.combat.enabled = !config.combat.enabled;
			b.setMessage(enabledTitle());
			saveToServer();
		}).bounds(x, y, w, 20).build());
		enabled.setTooltip(Tooltip.create(Component.literal("Master toggle for combat system features.")));
		y += 26;

		Button antiRestock = addRenderableWidget(toggleButton(x, y, w, "Anti restock", () -> config.combat.antiRestock, v -> config.combat.antiRestock = v));
		antiRestock.setTooltip(Tooltip.create(Component.literal("Blocks chests/shulkers/enderchests while combat-tagged.")));
		y += 24;

		Button antiElytra = addRenderableWidget(toggleButton(x, y, w, "Anti elytra", () -> config.combat.antiElytra, v -> config.combat.antiElytra = v));
		antiElytra.setTooltip(Tooltip.create(Component.literal("Prevents starting elytra flight while combat-tagged.")));
		y += 28;

		tagSeconds = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("Combat tag seconds")));
		tagSeconds.setValue(Integer.toString(config.combat.tagSeconds));
		tagSeconds.setTooltip(Tooltip.create(Component.literal("How long players stay combat-tagged after PvP damage.")));
		y += 26;

		damageMultiplier = addRenderableWidget(new EditBox(font, x, y, w, 20, Component.literal("PvP damage multiplier")));
		damageMultiplier.setValue(Double.toString(config.combat.pvpDamageMultiplier));
		damageMultiplier.setTooltip(Tooltip.create(Component.literal("Scales only PvP damage. Example: 10.0 = 10x.")));

		addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose()).bounds(x, this.height - 32, 126, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Save"), b -> save()).bounds(x + 134, this.height - 32, 126, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private void save() {
		config.combat.tagSeconds = parseInt(tagSeconds.getValue(), config.combat.tagSeconds);
		config.combat.pvpDamageMultiplier = parseDouble(damageMultiplier.getValue(), config.combat.pvpDamageMultiplier);
		saveToServer();
	}

	private Component enabledTitle() {
		return Component.literal("Combat system: " + (config.combat.enabled ? "Enabled" : "Disabled"));
	}

	private Button toggleButton(int x, int y, int w, String label, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
		return Button.builder(Component.literal(label + ": " + (getter.getAsBoolean() ? "Enabled" : "Disabled")), b -> {
			boolean next = !getter.getAsBoolean();
			setter.accept(next);
			b.setMessage(Component.literal(label + ": " + (next ? "Enabled" : "Disabled")));
			saveToServer();
		}).bounds(x, y, w, 20).build();
	}

	private static int parseInt(String raw, int fallback) {
		try {
			return Integer.parseInt(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}

	private static double parseDouble(String raw, double fallback) {
		try {
			return Double.parseDouble(raw.trim());
		} catch (Exception ignored) {
			return fallback;
		}
	}
}

