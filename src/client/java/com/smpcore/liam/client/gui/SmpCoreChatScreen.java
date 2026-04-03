package com.smpcore.liam.client.gui;

import com.smpcore.liam.client.gui.widget.SmpCoreBackButton;
import com.smpcore.liam.client.gui.widget.SmpCoreCategoryList;
import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class SmpCoreChatScreen extends SmpCoreMenuBase {
	private SmpCoreCategoryList list;

	public SmpCoreChatScreen(SmpCoreMenuBase parent, SmpCoreConfig config) {
		super(Component.literal("Chat & Notices"), parent, config);
	}

	@Override
	protected void init() {
		int w = Math.min(420, this.width - 40);
		int left = (this.width - w) / 2;
		int top = 44;
		int listBottom = this.height - 44;

		list = addRenderableWidget(new SmpCoreCategoryList(this.minecraft, w, this.height, top, listBottom, 44));
		list.setLeftPos(left);

		addToggle(new ItemStack(Items.PAPER), "Action bar notices", "Send SMP Core notices to the action bar instead of chat.", () -> config.messages.actionBar, v -> config.messages.actionBar = v);

		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.CLOCK),
				Component.literal("Notice cooldown"),
				Component.literal("Minimum milliseconds between repeated notices (0 = never repeat)."),
				List.of(Component.literal("Affects combat, cooldown, ban, and other notices.")),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal("Notice cooldown"),
						Component.literal("Milliseconds"),
						Long.toString(config.messages.minMillisBetweenNotices),
						List.of(Component.literal("Minimum milliseconds between repeated notices (0 = never repeat).")),
						txt -> {
							try {
								config.messages.minMillisBetweenNotices = Math.max(0, Long.parseLong(txt.trim()));
							} catch (Exception ignored) {
							}
						})),
				() -> Component.literal(config.messages.minMillisBetweenNotices + " ms")
		));

		addToggle(new ItemStack(Items.OAK_SIGN), "Proximity chat", "Only players near you can see your messages.", () -> config.messages.proximityChatEnabled, v -> config.messages.proximityChatEnabled = v);
		list.addCategoryEntry(new SmpCoreCategoryList.CategoryEntry(
				new ItemStack(Items.COMPASS),
				Component.literal("Proximity chat radius"),
				Component.literal("Radius in blocks for proximity chat."),
				List.of(Component.literal("Example: 64"), Component.literal("Set <= 0 to effectively disable radius.")),
				() -> this.minecraft.setScreen(new SmpCoreEditValueScreen(this, config,
						Component.literal("Proximity chat radius"),
						Component.literal("Radius in blocks"),
						Double.toString(config.messages.proximityChatRadius),
						List.of(Component.literal("Example: 64")),
						txt -> {
							try {
								config.messages.proximityChatRadius = Math.max(0.0, Double.parseDouble(txt.trim()));
							} catch (Exception ignored) {
							}
						})),
				() -> Component.literal(trimDouble(config.messages.proximityChatRadius) + " blocks")
		));
		addToggle(new ItemStack(Items.COMMAND_BLOCK), "Proximity affects commands", "Apply proximity chat to /say and /me broadcasts.", () -> config.messages.proximityChatAffectsCommands, v -> config.messages.proximityChatAffectsCommands = v);
		addToggle(new ItemStack(Items.SPYGLASS), "Proximity include spectators", "If enabled, spectators can receive proximity chat.", () -> config.messages.proximityChatIncludeSpectators, v -> config.messages.proximityChatIncludeSpectators = v);
		addToggle(new ItemStack(Items.DIAMOND), "Proximity ops bypass", "If enabled, ops always receive proximity chat.", () -> config.messages.proximityChatOpsBypass, v -> config.messages.proximityChatOpsBypass = v);

		addRenderableWidget(new SmpCoreBackButton(10, this.height - 30, this::onClose));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderSmpBackground(graphics);
		renderSmpHeader(graphics, getTitle(), Component.literal("Chat behavior and SMP Core notices"));
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

	private static String trimDouble(double v) {
		String s = Double.toString(v);
		return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
	}
}

