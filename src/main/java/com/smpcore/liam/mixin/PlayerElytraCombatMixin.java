package com.smpcore.liam.mixin;

import com.smpcore.liam.combat.CombatState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerElytraCombatMixin {
	@Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
	private void smpcore$blockElytraInCombat(CallbackInfoReturnable<Boolean> cir) {
		if (!((Object) this instanceof ServerPlayer player)) {
			return;
		}
		if (!CombatState.antiElytra()) {
			return;
		}
		if (CombatState.isInCombat(player.getUUID())) {
			cir.setReturnValue(false);
		}
	}
}

