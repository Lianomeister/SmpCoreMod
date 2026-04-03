package com.smpcore.liam.mixin;

import com.smpcore.liam.feature.PvpRulesFeature;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityPvpRulesMixin {
	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void smpcore$blockForbiddenPvp(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!((Object) this instanceof ServerPlayer victim)) {
			return;
		}
		if (!(source.getEntity() instanceof ServerPlayer attacker)) {
			return;
		}
		if (attacker == victim) {
			return;
		}

		if (PvpRulesFeature.noAfkKilling() && PvpRulesFeature.isAfk(victim)) {
			PvpRulesFeature.notifyAttacker(attacker, "That player is AFK.");
			cir.setReturnValue(false);
			return;
		}

		if (PvpRulesFeature.noNakedKilling() && PvpRulesFeature.isNaked(victim)) {
			if (PvpRulesFeature.allowNakedVsNaked() && PvpRulesFeature.isNaked(attacker)) {
				return;
			}
			PvpRulesFeature.notifyAttacker(attacker, "No naked killing.");
			cir.setReturnValue(false);
		}
	}
}

