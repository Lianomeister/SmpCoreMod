package com.smpcore.liam.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class SmpCorePayloads {
	private SmpCorePayloads() {
	}

	public static void register() {
		PayloadTypeRegistry.playS2C().register(OpenAdminPayload.TYPE, OpenAdminPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(SaveConfigPayload.TYPE, SaveConfigPayload.STREAM_CODEC);
	}

	public record OpenAdminPayload(String configJson) implements CustomPacketPayload {
		public static final Type<OpenAdminPayload> TYPE = CustomPacketPayload.createType(SmpCoreNet.S2C_OPEN_ADMIN.toString());
		public static final StreamCodec<RegistryFriendlyByteBuf, OpenAdminPayload> STREAM_CODEC = CustomPacketPayload.codec(
				(payload, buf) -> buf.writeUtf(payload.configJson()),
				buf -> new OpenAdminPayload(buf.readUtf())
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record SaveConfigPayload(String configJson) implements CustomPacketPayload {
		public static final Type<SaveConfigPayload> TYPE = CustomPacketPayload.createType(SmpCoreNet.C2S_SAVE_CONFIG.toString());
		public static final StreamCodec<RegistryFriendlyByteBuf, SaveConfigPayload> STREAM_CODEC = CustomPacketPayload.codec(
				(payload, buf) -> buf.writeUtf(payload.configJson()),
				buf -> new SaveConfigPayload(buf.readUtf())
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}
