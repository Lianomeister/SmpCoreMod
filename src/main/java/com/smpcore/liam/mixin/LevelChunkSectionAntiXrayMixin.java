package com.smpcore.liam.mixin;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.feature.AntiXrayFeature;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.Strategy;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionAntiXrayMixin {
	private static final Set<Block> DEEPSLATE_ORES = Set.of(
			Blocks.DEEPSLATE_COAL_ORE,
			Blocks.DEEPSLATE_COPPER_ORE,
			Blocks.DEEPSLATE_IRON_ORE,
			Blocks.DEEPSLATE_GOLD_ORE,
			Blocks.DEEPSLATE_LAPIS_ORE,
			Blocks.DEEPSLATE_REDSTONE_ORE,
			Blocks.DEEPSLATE_DIAMOND_ORE,
			Blocks.DEEPSLATE_EMERALD_ORE
	);

	private static final Set<Block> HIDE_FAST = Set.of(
			Blocks.DIAMOND_ORE,
			Blocks.DEEPSLATE_DIAMOND_ORE,
			Blocks.EMERALD_ORE,
			Blocks.DEEPSLATE_EMERALD_ORE,
			Blocks.ANCIENT_DEBRIS
	);

	private static final Set<Block> HIDE_BASIC = Set.of(
			Blocks.DIAMOND_ORE,
			Blocks.DEEPSLATE_DIAMOND_ORE,
			Blocks.EMERALD_ORE,
			Blocks.DEEPSLATE_EMERALD_ORE,
			Blocks.ANCIENT_DEBRIS,
			Blocks.NETHER_GOLD_ORE,
			Blocks.NETHER_QUARTZ_ORE
	);

	private static final Set<Block> HIDE_STRICT = Set.of(
			Blocks.COAL_ORE,
			Blocks.COPPER_ORE,
			Blocks.IRON_ORE,
			Blocks.GOLD_ORE,
			Blocks.LAPIS_ORE,
			Blocks.REDSTONE_ORE,
			Blocks.DIAMOND_ORE,
			Blocks.EMERALD_ORE,
			Blocks.DEEPSLATE_COAL_ORE,
			Blocks.DEEPSLATE_COPPER_ORE,
			Blocks.DEEPSLATE_IRON_ORE,
			Blocks.DEEPSLATE_GOLD_ORE,
			Blocks.DEEPSLATE_LAPIS_ORE,
			Blocks.DEEPSLATE_REDSTONE_ORE,
			Blocks.DEEPSLATE_DIAMOND_ORE,
			Blocks.DEEPSLATE_EMERALD_ORE,
			Blocks.NETHER_GOLD_ORE,
			Blocks.NETHER_QUARTZ_ORE,
			Blocks.ANCIENT_DEBRIS,
			Blocks.SPAWNER
	);

	@Shadow
	@Final
	private PalettedContainer<BlockState> states;

	@Shadow
	@Final
	private PalettedContainerRO<Holder<Biome>> biomes;

	@Shadow
	private short nonEmptyBlockCount;

	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	private void smpcore$antiXray$writeObfuscated(FriendlyByteBuf buf, CallbackInfo ci) {
		AntiXrayFeature.Context ctx = AntiXrayFeature.context();
		if (ctx == null) {
			return;
		}

		SmpCoreConfig.AntiXrayMode mode = ctx.mode();
		if (mode == null) {
			return;
		}

		Set<Block> hidden = AntiXrayFeature.useCustomHiddenBlocks() ? AntiXrayFeature.customHiddenBlocks() : hiddenSet(mode);
		if (hidden.isEmpty()) {
			return;
		}
		if (!AntiXrayFeature.hideSpawners() && hidden.contains(Blocks.SPAWNER)) {
			java.util.HashSet<Block> copy = new java.util.HashSet<>(hidden);
			copy.remove(Blocks.SPAWNER);
			hidden = copy;
			if (hidden.isEmpty()) {
				return;
			}
		}
		final Set<Block> hiddenFinal = hidden;

		// Fast bail-out: don't allocate/copy if this section can't contain anything we hide.
		if (!states.maybeHas(state -> hiddenFinal.contains(state.getBlock()))) {
			return;
		}

		buf.writeShort(nonEmptyBlockCount);

		PalettedContainer<BlockState> out = new PalettedContainer<>(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY));
		for (int y = 0; y < 16; y++) {
			for (int z = 0; z < 16; z++) {
				for (int x = 0; x < 16; x++) {
					BlockState original = states.get(x, y, z);
					out.set(x, y, z, obfuscate(original, x, y, z, mode, hiddenFinal, ctx.level()));
				}
			}
		}

		out.write(buf);
		biomes.write(buf);
		ci.cancel();
	}

	private BlockState obfuscate(BlockState original, int x, int y, int z, SmpCoreConfig.AntiXrayMode mode, Set<Block> hidden, Level level) {
		Block block = original.getBlock();
		if (!hidden.contains(block)) {
			return original;
		}
		if (mode != SmpCoreConfig.AntiXrayMode.STRICT && AntiXrayFeature.exposeCheck() && isExposedToAirOrFluid(x, y, z, mode)) {
			return original;
		}
		return replacementFor(original, level);
	}

	private boolean isExposedToAirOrFluid(int x, int y, int z, SmpCoreConfig.AntiXrayMode mode) {
		return isExposedNeighbor(x + 1, y, z, mode)
				|| isExposedNeighbor(x - 1, y, z, mode)
				|| isExposedNeighbor(x, y + 1, z, mode)
				|| isExposedNeighbor(x, y - 1, z, mode)
				|| isExposedNeighbor(x, y, z + 1, mode)
				|| isExposedNeighbor(x, y, z - 1, mode);
	}

	private boolean isExposedNeighbor(int x, int y, int z, SmpCoreConfig.AntiXrayMode mode) {
		boolean inBounds = x >= 0 && x < 16 && y >= 0 && y < 16 && z >= 0 && z < 16;
		if (!inBounds) {
			// BASIC/FAST: treat boundaries as "potentially exposed" to avoid over-hiding in caves.
			// STRICT: hide more aggressively.
			return mode != SmpCoreConfig.AntiXrayMode.STRICT;
		}
		BlockState neighbor = states.get(x, y, z);
		return neighbor.isAir() || !neighbor.getFluidState().isEmpty();
	}

	private static BlockState replacementFor(BlockState original, Level level) {
		if (level.dimension().equals(Level.NETHER)) {
			return Blocks.NETHERRACK.defaultBlockState();
		}
		if (level.dimension().equals(Level.END)) {
			return Blocks.END_STONE.defaultBlockState();
		}
		if (DEEPSLATE_ORES.contains(original.getBlock())) {
			return Blocks.DEEPSLATE.defaultBlockState();
		}
		return Blocks.STONE.defaultBlockState();
	}

	private static Set<Block> hiddenSet(SmpCoreConfig.AntiXrayMode mode) {
		return switch (mode) {
			case FAST -> HIDE_FAST;
			case BASIC -> HIDE_BASIC;
			case STRICT -> HIDE_STRICT;
		};
	}
}
