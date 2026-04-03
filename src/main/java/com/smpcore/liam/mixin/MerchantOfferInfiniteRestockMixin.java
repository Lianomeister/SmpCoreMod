package com.smpcore.liam.mixin;

import com.smpcore.liam.feature.VillagerRestockFeature;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantOffer.class)
public class MerchantOfferInfiniteRestockMixin {
	@Inject(method = "isOutOfStock", at = @At("HEAD"), cancellable = true)
	private void smpcore$infiniteRestock(CallbackInfoReturnable<Boolean> cir) {
		if (VillagerRestockFeature.infiniteRestock()) {
			cir.setReturnValue(false);
		}
	}
}

