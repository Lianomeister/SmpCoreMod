package com.smpcore.liam.mixin;

import com.smpcore.liam.feature.CraftingRulesFeature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingMenu.class)
public class CraftingMenuOneCraftMixin {
	@Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
	private void smpcore$blockShiftCraft(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
		if (!CraftingRulesFeature.oneCraftRecipes()) {
			return;
		}
		// Slot 0 is the crafting result; blocking quick-move prevents "craft all" via shift-click.
		if (index == 0) {
			cir.setReturnValue(ItemStack.EMPTY);
		}
	}
}

