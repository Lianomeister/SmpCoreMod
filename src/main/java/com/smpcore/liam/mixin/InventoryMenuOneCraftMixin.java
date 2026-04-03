package com.smpcore.liam.mixin;

import com.smpcore.liam.feature.CraftingRulesFeature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryMenu.class)
public class InventoryMenuOneCraftMixin {
	@Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
	private void smpcore$blockShiftCraft(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
		if (!CraftingRulesFeature.oneCraftRecipes()) {
			return;
		}
		// Slot 0 is the 2x2 crafting result in the player inventory menu.
		if (index == 0) {
			cir.setReturnValue(ItemStack.EMPTY);
		}
	}
}

