package com.smpcore.liam.mixin;

import com.smpcore.liam.feature.BreachSwapBanFeature;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class BreachSwapBanPacketMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleSetCarriedItem", at = @At("HEAD"), cancellable = true)
	private void smpcore$blockBreachSwapping(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
		if (!BreachSwapBanFeature.shouldBlockSwap(player)) {
			return;
		}
		BreachSwapBanFeature.notifyBlocked(player);
		ci.cancel();
	}
}

