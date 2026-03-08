package com.smpcore.liam.client.gui;

import com.smpcore.liam.config.ConfigJson;
import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.net.SmpCorePayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class SmpCoreMenuBase extends Screen {
	protected final SmpCoreConfig config;
	protected final Screen parent;

	protected SmpCoreMenuBase(Component title, Screen parent, SmpCoreConfig config) {
		super(title);
		this.parent = parent;
		this.config = config;
	}

	protected void saveToServer() {
		ClientPlayNetworking.send(new SmpCorePayloads.SaveConfigPayload(ConfigJson.toJson(config)));
	}

	@Override
	public void onClose() {
		if (parent != null) {
			this.minecraft.setScreen(parent);
		} else {
			super.onClose();
		}
	}
}

