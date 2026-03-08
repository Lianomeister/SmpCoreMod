package com.smpcore.liam.mixin;

import com.smpcore.liam.feature.ProximityChatFeature;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(PlayerList.class)
public abstract class PlayerListProximityChatMixin {
	@Invoker("broadcastChatMessage")
	abstract void smpcore$broadcastChatMessage(PlayerChatMessage message, Predicate<ServerPlayer> filter, ServerPlayer sender, ChatType.Bound bound);

	@Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At("HEAD"), cancellable = true)
	private void smpcore$proximityChat(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound bound, CallbackInfo ci) {
		if (!ProximityChatFeature.enabled()) {
			return;
		}
		if (sender == null) {
			return;
		}
		smpcore$broadcastChatMessage(message, p -> ProximityChatFeature.isInRange(sender, p), sender, bound);
		ci.cancel();
	}

	@Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At("HEAD"), cancellable = true)
	private void smpcore$proximityChatCommand(PlayerChatMessage message, CommandSourceStack source, ChatType.Bound bound, CallbackInfo ci) {
		if (!ProximityChatFeature.enabled()) {
			return;
		}
		if (!ProximityChatFeature.affectsCommands()) {
			return;
		}
		if (source == null) {
			return;
		}
		ServerPlayer sender = source.getPlayer();
		if (sender == null) {
			return;
		}
		smpcore$broadcastChatMessage(message, p -> ProximityChatFeature.isInRange(sender, p), sender, bound);
		ci.cancel();
	}
}
