package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.client.gui.widget.SmpCoreStyledButton;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class SmpCoreRecipesScreen extends SmpCoreMenuBase {
	private SmpCoreStyledButton enabledButton;
	private MultiLineEditBox shapeless;

	public SmpCoreRecipesScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Recipes"), parent, config);
	}

	@Override
	protected void init() {
		int w = 420;
		int x = (this.width - w) / 2;
		int y = 44;

		enabledButton = addRenderableWidget(new SmpCoreStyledButton(
				x, y, w, 20,
				enabledTitle(),
				new ItemStack(Items.KNOWLEDGE_BOOK),
				() -> {
					config.recipes.enabled = !config.recipes.enabled;
					enabledButton.setMessage(enabledTitle());
					saveToServer();
				}
		));
		y += 30;

		shapeless = new MultiLineEditBox.Builder()
				.setX(x)
				.setY(y)
				.setShowBackground(true)
				.setPlaceholder(Component.literal(
						"# One shapeless recipe per line:\n" +
						"# <output_id> <count> <- <input_id> <count>, <input_id> <count>\n" +
						"# Example:\n" +
						"minecraft:warped_planks 4 <- minecraft:warped_stem 1"
				))
				.build(font, w, 140, Component.literal("Shapeless recipes"));
		shapeless.setValue(String.join("\n", config.recipes.shapeless));
		addRenderableWidget(shapeless);

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderSmpBackground(graphics);
		renderSmpHeader(graphics, getTitle(), Component.literal("Define recipes, then run /smpcore recipes install"));
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void onClose() {
		save();
		super.onClose();
	}

	private void save() {
		config.recipes.shapeless = parseLines(shapeless.getValue());
		saveToServer();
	}

	private Component enabledTitle() {
		return Component.literal("Custom recipes: " + (config.recipes.enabled ? "Enabled" : "Disabled"));
	}

	private static List<String> parseLines(String raw) {
		if (raw == null || raw.isBlank()) {
			return List.of();
		}
		return raw.lines()
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.filter(s -> !s.startsWith("#"))
				.toList();
	}
}

