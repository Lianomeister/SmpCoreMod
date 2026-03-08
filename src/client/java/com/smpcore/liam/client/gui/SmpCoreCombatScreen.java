package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreCategoryList;
import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class SmpCoreCombatScreen extends SmpCoreMenuBase {
	private SmpCoreCategoryList list;

	public SmpCoreCombatScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Combat"), parent, config);
	}

	@Override
	protected void init() {
		int w = Math.min(420, this.width - 40);
		int left = (this.width - w) / 2;
		int top = 44;
		int listBottom = this.height - 44;

		list = addRenderableWidget(new SmpCoreCategoryList(this.minecraft, w, this.height, top, listBottom, 44));
		list.setLeftPos(left);

		addToggle(new ItemStack(Items.SHIELD), "Combat system", "Master toggle for combat system features.", () -> config.combat.enabled, v -> config.combat.enabled = v);
		addToggle(new ItemStack(Items.CHEST), "Anti restock", "Blocks chests/shulkers/enderchests while combat-tagged.", () -> config.combat.antiRestock, v -> config.combat.antiRestock = v);
		addToggle(new ItemStack(Items.ELYTRA), "Anti elytra", "Prevents starting elytra flight while combat-tagged.", () -> config.combat.antiElytra, v -> config.combat.antiElytra = v);

		addInt(new ItemStack(Items.CLOCK), "Combat tag seconds", "How long players stay combat-tagged after PvP damage.", () -> config.combat.tagSeconds, v -> config.combat.tagSeconds = v, 0, 300, "s");

		addDouble(new ItemStack(Items.IRON_SWORD), "Player damage multiplier", "Scales ALL damage taken by players. Example: 10.0 = 10x.", () -> config.combat.playerDamageMultiplier, v -> config.combat.playerDamageMultiplier = v, 0.0, 100.0);
		addDouble(new ItemStack(Items.NETHERITE_SWORD), "PvP damage multiplier", "Scales only PvP damage. Example: 10.0 = 10x.", () -> config.combat.pvpDamageMultiplier, v -> config.combat.pvpDamageMultiplier = v, 0.0, 100.0);

		addInt(new ItemStack(Items.SHIELD), "Shield cooldown (block)", "Adds a shield cooldown whenever damage is blocked.", () -> config.combat.shieldCooldownSeconds, v -> config.combat.shieldCooldownSeconds = v, 0, 60, "s");
		addToggle(new ItemStack(Items.MACE), "Mace stuns shield", "If the shield blocks a mace hit, the shield gets stunned.", () -> config.combat.maceStunsShield, v -> config.combat.maceStunsShield = v);
		addInt(new ItemStack(Items.MACE), "Mace shield stun", "Shield stun duration after a blocked mace hit.", () -> config.combat.maceShieldStunSeconds, v -> config.combat.maceShieldStunSeconds = v, 0, 60, "s");

		addToggle(new ItemStack(Items.ENCHANTED_BOOK), "Ban breach swapping", "Blocks swapping hotbar slot briefly after Breach attacks.", () -> config.combat.banBreachSwapping, v -> config.combat.banBreachSwapping = v);
		addInt(new ItemStack(Items.ENCHANTED_BOOK), "Breach swap lock", "Time (ms) where hotbar slot swapping is blocked after Breach attacks.", () -> config.combat.breachSwapLockMs, v -> config.combat.breachSwapLockMs = v, 0, 5000, "ms");

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.literal("Combat tag, damage scaling, and PvP rules"), width / 2, 30, 0xB9B9B9);
		super.render(graphics, mouseX, mouseY, partialTick);
		if (list != null) {
			List<Component> tooltip = list.consumeHoveredTooltip();
			if (tooltip != null) {
				graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
			}
		}
	}

	private void addToggle(ItemStack icon, String title, String desc, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				icon,
				Component.literal(title),
				Component.literal(desc),
				List.of(Component.literal(desc)),
				() -> {
					boolean next = !getter.getAsBoolean();
					setter.accept(next);
					saveToServer();
				},
				() -> Component.literal(getter.getAsBoolean() ? "Enabled" : "Disabled")
		));
	}

	private void addInt(ItemStack icon, String title, String desc, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter, int min, int max, String suffix) {
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				icon,
				Component.literal(title),
				Component.literal(desc),
				List.of(Component.literal(desc), Component.literal("Range: " + min + " .. " + max)),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal(title),
						Component.literal("Enter a number"),
						Integer.toString(getter.getAsInt()),
						List.of(Component.literal(desc), Component.literal("Range: " + min + " .. " + max)),
						raw -> {
							int v = clamp(parseInt(raw, getter.getAsInt()), min, max);
							setter.accept(v);
						})),
				() -> Component.literal(getter.getAsInt() + suffix)
		));
	}

	private void addDouble(ItemStack icon, String title, String desc, java.util.function.DoubleSupplier getter, java.util.function.DoubleConsumer setter, double min, double max) {
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				icon,
				Component.literal(title),
				Component.literal(desc),
				List.of(Component.literal(desc), Component.literal("Range: " + min + " .. " + max)),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal(title),
						Component.literal("Enter a number"),
						Double.toString(getter.getAsDouble()),
						List.of(Component.literal(desc), Component.literal("Range: " + min + " .. " + max)),
						raw -> {
							double v = clamp(parseDouble(raw, getter.getAsDouble()), min, max);
							setter.accept(v);
						})),
				() -> Component.literal(trimDouble(getter.getAsDouble()) + "x")
		));
	}

	private static int clamp(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}

	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}

	private static String trimDouble(double v) {
		String s = Double.toString(v);
		return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
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
