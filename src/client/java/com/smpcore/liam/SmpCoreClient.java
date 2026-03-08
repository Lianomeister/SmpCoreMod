package com.smpcore.liam;

import com.smpcore.liam.client.gui.SmpCoreMainMenuScreen;
import com.smpcore.liam.client.gui.SmpCoreMissingVoiceChatScreen;
import com.smpcore.liam.client.gui.SmpCoreVoiceChatClientScreen;
import com.smpcore.liam.config.ConfigJson;
import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.net.SmpCorePayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class SmpCoreClient implements ClientModInitializer {
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(SmpCore.MOD_ID, "menu"));
	private static KeyMapping openMenuKey;
	private static KeyMapping openVoiceChatClientKey;
	private static boolean shownMissingVoiceChat;

	@Override
	public void onInitializeClient() {
		openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.smpcore.open_menu", GLFW.GLFW_KEY_UNKNOWN, CATEGORY));
		openVoiceChatClientKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.smpcore.open_voicechat_client", GLFW.GLFW_KEY_UNKNOWN, CATEGORY));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!shownMissingVoiceChat && !isVoiceChatInstalled() && client.screen instanceof TitleScreen) {
				shownMissingVoiceChat = true;
				client.setScreen(new SmpCoreMissingVoiceChatScreen(client.screen));
			}

			while (openMenuKey.consumeClick()) {
				if (client.player == null) {
					continue;
				}
				if (ClientPlayNetworking.canSend(SmpCorePayloads.RequestOpenAdminPayload.TYPE)) {
					ClientPlayNetworking.send(new SmpCorePayloads.RequestOpenAdminPayload());
				} else {
					// Fallback for non-play screens / no server support: try the command.
					client.player.connection.sendCommand("smpcore");
				}
			}
			while (openVoiceChatClientKey.consumeClick()) {
				if (!isVoiceChatInstalled()) {
					client.setScreen(new SmpCoreMissingVoiceChatScreen(client.screen));
				} else {
					if (client.player == null) {
						continue;
					}
					client.setScreen(new SmpCoreVoiceChatClientScreen(client.screen, new SmpCoreConfig()));
				}
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(SmpCorePayloads.OpenAdminPayload.TYPE, (payload, context) -> {
			Minecraft client = Minecraft.getInstance();
			client.execute(() -> {
				try {
					var config = ConfigJson.fromJson(payload.configJson());
					Minecraft.getInstance().setScreen(new SmpCoreMainMenuScreen(config));
				} catch (Exception e) {
					SmpCore.LOGGER.warn("Failed to open SMP Core admin screen: invalid config JSON from server", e);
				}
			});
		});
	}

	private static boolean isVoiceChatInstalled() {
		// Simple Voice Chat (by henkelmax) uses the mod id "voicechat" on Fabric.
		// Keep a small fallback list because some forks use different ids.
		return FabricLoader.getInstance().isModLoaded("voicechat")
				|| FabricLoader.getInstance().isModLoaded("simplevoicechat");
	}
}
