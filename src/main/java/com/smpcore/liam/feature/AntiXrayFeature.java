package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public final class AntiXrayFeature {
	private static volatile boolean enabled;
	private static volatile SmpCoreConfig.AntiXrayMode mode;

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
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static SmpCoreConfig.AntiXrayMode mode() {
		return mode;
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
}

