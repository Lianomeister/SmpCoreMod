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
		double mult = CombatState.pvpDamageMultiplier();
		if (mult == 1.0) {
			return amount;
		}

		if (!((Object) this instanceof Player)) {
			return amount;
		}
		if (!(source.getEntity() instanceof Player)) {
			return amount;
		}

		return (float) (amount * mult);
	}
}

