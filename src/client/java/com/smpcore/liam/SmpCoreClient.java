package com.smpcore.liam;

import com.smpcore.liam.client.gui.SmpCoreAdminScreen;
import com.smpcore.liam.config.ConfigJson;
import com.smpcore.liam.net.SmpCorePayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class SmpCoreClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SmpCorePayloads.OpenAdminPayload.TYPE, (payload, context) -> {
			Minecraft client = Minecraft.getInstance();
			client.execute(() -> {
				try {
					var config = ConfigJson.fromJson(payload.configJson());
					Minecraft.getInstance().setScreen(new SmpCoreAdminScreen(config));
				} catch (Exception e) {
					SmpCore.LOGGER.warn("Failed to open SMP Core admin screen: invalid config JSON from server", e);
				}
			});
		});
	}
}
