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
		PayloadTypeRegistry.playC2S().register(RequestOpenAdminPayload.TYPE, RequestOpenAdminPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(SaveConfigPayload.TYPE, SaveConfigPayload.STREAM_CODEC);
	}

	public record RequestOpenAdminPayload() implements CustomPacketPayload {
		// NOTE: createType(String) expects a *path* (no namespace). Use a unique prefix to avoid collisions.
		public static final Type<RequestOpenAdminPayload> TYPE = CustomPacketPayload.createType("smpcore_request_open_admin");
		public static final StreamCodec<RegistryFriendlyByteBuf, RequestOpenAdminPayload> STREAM_CODEC = CustomPacketPayload.codec(
				(payload, buf) -> {
				},
				buf -> new RequestOpenAdminPayload()
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record OpenAdminPayload(String configJson) implements CustomPacketPayload {
		// NOTE: createType(String) expects a *path* (no namespace). Use a unique prefix to avoid collisions.
		public static final Type<OpenAdminPayload> TYPE = CustomPacketPayload.createType("smpcore_open_admin");
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
		// NOTE: createType(String) expects a *path* (no namespace). Use a unique prefix to avoid collisions.
		public static final Type<SaveConfigPayload> TYPE = CustomPacketPayload.createType("smpcore_save_config");
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
