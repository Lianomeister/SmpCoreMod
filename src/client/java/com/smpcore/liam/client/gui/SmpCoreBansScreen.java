package com.smpcore.liam.client.gui;

import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class SmpCoreBansScreen extends SmpCoreMenuBase {
	public SmpCoreBansScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Bans"), parent, config);
	}

	@Override
	protected void init() {
		int w = 240;
		int x = (this.width - w) / 2;
		int y = 44;

		addRenderableWidget(toggleBanButton(x, y, w, "Bed bombing", () -> config.bans.banBedBombing, v -> config.bans.banBedBombing = v));
		y += 24;
		addRenderableWidget(toggleBanButton(x, y, w, "Anchor bombing", () -> config.bans.banAnchorBombing, v -> config.bans.banAnchorBombing = v));
		y += 24;
		addRenderableWidget(toggleBanButton(x, y, w, "TNT minecarts", () -> config.bans.banTntMinecarts, v -> config.bans.banTntMinecarts = v));
		y += 24;
		addRenderableWidget(toggleBanButton(x, y, w, "Mace", () -> config.bans.banMace, v -> config.bans.banMace = v));
		y += 24;
		addRenderableWidget(toggleBanButton(x, y, w, "Ender pearls", () -> config.bans.banPearls, v -> config.bans.banPearls = v));
		y += 24;
		addRenderableWidget(toggleBanButton(x, y, w, "End crystals", () -> config.bans.banCrystals, v -> config.bans.banCrystals = v));
		y += 24;
		addRenderableWidget(toggleBanButton(x, y, w, "Tipped arrows", () -> config.bans.banTippedArrows, v -> config.bans.banTippedArrows = v));

		addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose()).bounds(x, this.height - 32, 116, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Save"), b -> saveToServer()).bounds(x + 124, this.height - 32, 116, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fillGradient(0, 0, width, height, 0xFF140B22, 0xFF0A0F25);
		graphics.drawCenteredString(font, getTitle(), width / 2, 18, 0xFFFFFF);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private Button toggleBanButton(int x, int y, int w, String label, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
		return Button.builder(title(label, getter.getAsBoolean()), b -> {
			boolean next = !getter.getAsBoolean();
			setter.accept(next);
			b.setMessage(title(label, next));
			saveToServer();
		}).bounds(x, y, w, 20).build();
	}

	private static Component title(String label, boolean banned) {
		return Component.literal(label + ": " + (banned ? "Banned" : "Allowed"));
	}
}

