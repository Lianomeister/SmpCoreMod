package com.smpcore.liam.mixin;

import com.smpcore.liam.SmpCore;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayer.class)
public abstract class AnonymousInvisDeathMessageMixin {
	@ModifyArg(
			method = "die",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
			),
			index = 0
	)
	private Component smpcore$hideInvisibleKillerName(Component original, DamageSource source) {
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

