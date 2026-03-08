package com.smpcore.liam.mixin;

import com.smpcore.liam.combat.CombatState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
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

			double maceCap = CombatState.maceDamageCap();
			if (maceCap > 0.0 && source.getEntity() instanceof Player attacker && attacker.getMainHandItem().is(Items.MACE)) {
				out = Math.min(out, maceCap);
			}
		}

		if (CombatState.immortalityEnabled()) {
			if (CombatState.immortalityAllowVoidDeath() && source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
				return (float) out;
			}
			Player self = (Player) (Object) this;
			boolean holdingTotem =
					self.getMainHandItem().is(Items.TOTEM_OF_UNDYING)
							|| self.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
			if (!holdingTotem) {
				float health = ((LivingEntity) (Object) this).getHealth();
				double minHealth = CombatState.immortalityMinHealth();
				double maxDamage = Math.max(0.0, health - minHealth);
				out = Math.min(out, maxDamage);
			}
		}

		return (float) out;
	}
}
