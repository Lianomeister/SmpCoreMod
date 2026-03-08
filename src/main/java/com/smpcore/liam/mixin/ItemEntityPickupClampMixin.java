package com.smpcore.liam.mixin;

import com.smpcore.liam.feature.EnchantmentRulesFeature;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityPickupClampMixin {
	@Inject(method = "playerTouch", at = @At("HEAD"))
	private void smpcore$clampEnchantmentsOnPickup(Player player, CallbackInfo ci) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		ItemEntity self = (ItemEntity) (Object) this;
		var stack = self.getItem();
		if (stack == null || stack.isEmpty()) {
			return;
		}
		if (EnchantmentRulesFeature.enforceOnStack(serverPlayer, stack)) {
			self.setItem(stack);
		}
	}
}

