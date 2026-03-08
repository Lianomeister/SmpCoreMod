package com.smpcore.liam.mixin;

import com.smpcore.liam.feature.EnderChestLockFeature;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class EnderChestLockedItemsClickMixin {
	@Shadow
	@Final
	private ServerPlayer player;

	@Inject(
			method = "handleContainerClick",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;suppressRemoteUpdates()V"),
			cancellable = true
	)
	private void smpcore$lockEnderChestItems(ServerboundContainerClickPacket packet, CallbackInfo ci) {
		if (!EnderChestLockFeature.isEnabled()) {
			return;
		}

		AbstractContainerMenu menu = player.containerMenu;
		int slotNum = packet.slotNum();
		if (!menu.isValidSlotIndex(slotNum)) {
			return;
		}

		Slot clicked = menu.getSlot(slotNum);
		boolean destIsEnderChest = clicked.container instanceof PlayerEnderChestContainer;
		boolean menuHasEnderChest = destIsEnderChest;
		if (!menuHasEnderChest) {
			for (Slot s : menu.slots) {
				if (s.container instanceof PlayerEnderChestContainer) {
					menuHasEnderChest = true;
					break;
				}
			}
		}

		if (!menuHasEnderChest) {
			return;
		}

		ClickType type = packet.clickType();
		boolean blocked = false;

		if (type == ClickType.QUICK_MOVE) {
			// Shift-click from player inventory -> tries to move into the open container.
			if (!destIsEnderChest) {
				ItemStack source = clicked.getItem();
				blocked = EnderChestLockFeature.isLocked(source);
			}
		} else if (type == ClickType.SWAP) {
			// Number-key hotbar swap into a container slot.
			if (destIsEnderChest) {
				int hotbar = packet.buttonNum();
				if (hotbar >= 0 && hotbar < 9) {
					ItemStack from = player.getInventory().getItem(hotbar);
					blocked = EnderChestLockFeature.isLocked(from);
				}
			}
		} else if (type != ClickType.PICKUP_ALL && type != ClickType.CLONE && type != ClickType.THROW) {
			// Normal pickup / drag: block only when trying to place the carried stack into ender chest.
			if (destIsEnderChest) {
				ItemStack carried = menu.getCarried();
				blocked = EnderChestLockFeature.isLocked(carried);
			}
		}

		if (!blocked) {
			return;
		}

		EnderChestLockFeature.notify(player, "This item can't be put into an Ender Chest.");
		menu.sendAllDataToRemote();
		ci.cancel();
	}
}

