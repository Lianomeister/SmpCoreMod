package com.smpcore.liam.mixin;

import com.smpcore.liam.feature.AntiXrayFeature;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundLevelChunkPacketData.class)
public final class ClientboundLevelChunkPacketDataAntiXrayMixin {
	@Inject(method = "extractChunkData", at = @At("HEAD"))
	private static void smpcore$antiXray$pushCtx(FriendlyByteBuf buf, LevelChunk chunk, CallbackInfo ci) {
		AntiXrayFeature.pushContext(chunk);
	}

	@Inject(method = "extractChunkData", at = @At("RETURN"))
	private static void smpcore$antiXray$popCtx(FriendlyByteBuf buf, LevelChunk chunk, CallbackInfo ci) {
		AntiXrayFeature.popContext();
	}
}

