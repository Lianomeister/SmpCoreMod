package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.IdUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public final class AntiXrayFeature {
	private static volatile boolean enabled;
	private static volatile SmpCoreConfig.AntiXrayMode mode;
	private static volatile boolean exposeCheck;
	private static volatile boolean hideSpawners;
	private static volatile boolean useCustomHiddenBlocks;
	private static volatile Set<Block> customHiddenBlocks = Set.of();

	private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

	public record Context(Level level, SmpCoreConfig.AntiXrayMode mode) {
	}

	private AntiXrayFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
	}

	public static void reload(SmpCoreConfig config) {
		enabled = config.gameplay.antiXrayEnabled;
		mode = config.gameplay.antiXrayMode;
		exposeCheck = config.gameplay.antiXrayExposeCheck;
		hideSpawners = config.gameplay.antiXrayHideSpawners;
		useCustomHiddenBlocks = config.gameplay.antiXrayUseCustomHiddenBlocks;
		customHiddenBlocks = buildCustomHiddenBlocks(config);
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static SmpCoreConfig.AntiXrayMode mode() {
		return mode;
	}

	public static boolean exposeCheck() {
		return exposeCheck;
	}

	public static boolean hideSpawners() {
		return hideSpawners;
	}

	public static boolean useCustomHiddenBlocks() {
		return useCustomHiddenBlocks && !customHiddenBlocks.isEmpty();
	}

	public static Set<Block> customHiddenBlocks() {
		return customHiddenBlocks;
	}

	public static void pushContext(LevelChunk chunk) {
		if (!enabled) {
			return;
		}
		CONTEXT.set(new Context(chunk.getLevel(), mode));
	}

	public static void popContext() {
		CONTEXT.remove();
	}

	public static Context context() {
		return CONTEXT.get();
	}

	private static Set<Block> buildCustomHiddenBlocks(SmpCoreConfig config) {
		Set<Block> out = new HashSet<>();
		for (String rawId : config.gameplay.antiXrayCustomHiddenBlocks) {
			Identifier id = IdUtil.parse(rawId);
			if (id == null) {
				continue;
			}
			Block block = BuiltInRegistries.BLOCK.getValue(id);
			if (block != null && block != Blocks.AIR) {
				out.add(block);
			}
		}
		return out;
	}
}
