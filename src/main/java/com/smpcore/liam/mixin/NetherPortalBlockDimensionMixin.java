package com.smpcore.liam.mixin;

import com.smpcore.liam.feature.DimensionRulesFeature;
import com.smpcore.liam.util.NoticeCooldowns;
import com.smpcore.liam.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockDimensionMixin {
	private static final NoticeCooldowns NOTICE_COOLDOWNS = new NoticeCooldowns();

	@Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
	private void smpcore$blockPortalIfNetherDisabled(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean bl, CallbackInfo ci) {
		if (DimensionRulesFeature.allowNether()) {
			return;
		}
		if (!(entity instanceof ServerPlayer player)) {
			return;
		}
		// Only block entering the nether from the overworld.
		if (!level.dimension().equals(Level.OVERWORLD)) {
			return;
		}
		if (NOTICE_COOLDOWNS.shouldNotify(player.getUUID(), 1500)) {
			TextUtil.actionBar(player, "The Nether is disabled on this server.");
		}
		ci.cancel();
	}
}

