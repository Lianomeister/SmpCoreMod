package com.smpcore.liam.mixin;

import com.smpcore.liam.combat.CombatState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityDamageScaleMixin {
	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float smpcore$scalePvpDamage(float amount, ServerLevel level, DamageSource source) {
		if (!((Object) this instanceof Player)) {
			return amount;
		}

		double out = amount;

		double playerMult = CombatState.playerDamageMultiplier();
		if (playerMult != 1.0) {
			out *= playerMult;
		}

		if (source.getEntity() instanceof Player) {
			double pvpMult = CombatState.pvpDamageMultiplier();
			if (pvpMult != 1.0) {
				out *= pvpMult;
			}
		}

		return (float) out;
	}
}
