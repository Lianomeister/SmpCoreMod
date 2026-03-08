package com.smpcore.liam.mixin;

import com.smpcore.liam.SmpCore;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class AnonymousInvisDeathMessageMixin {
	private static final ThreadLocal<DamageSource> SMPCORE_DEATH_SOURCE = new ThreadLocal<>();

	@Inject(method = "die", at = @At("HEAD"))
	private void smpcore$captureDeathSource(DamageSource source, CallbackInfo ci) {
		SMPCORE_DEATH_SOURCE.set(source);
	}

	@Inject(method = "die", at = @At("RETURN"))
	private void smpcore$clearDeathSource(DamageSource source, CallbackInfo ci) {
		SMPCORE_DEATH_SOURCE.remove();
	}

	@ModifyArg(
			method = "die",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
			),
			index = 0
	)
	private Component smpcore$hideInvisibleKillerName(Component original) {
		DamageSource source = SMPCORE_DEATH_SOURCE.get();
		if (source == null) {
			return original;
		}

		var cfg = SmpCore.getConfig();
		if (cfg == null || !cfg.gameplay.invisibilityAnonymousKills) {
			return original;
		}
		if (!(source.getEntity() instanceof ServerPlayer attacker)) {
			return original;
		}
		if (!attacker.hasEffect(MobEffects.INVISIBILITY)) {
			return original;
		}
		ServerPlayer victim = (ServerPlayer) (Object) this;
		return Component.translatable("smpcore.death.anonymous", victim.getDisplayName());
	}
}
