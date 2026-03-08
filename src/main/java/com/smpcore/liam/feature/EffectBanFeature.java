package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.IdUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;

import java.util.HashSet;
import java.util.Set;

public final class EffectBanFeature {
	private static boolean registered;

	private static volatile Set<Holder<MobEffect>> banned = Set.of();
	private static volatile int tickInterval = 40;

	private EffectBanFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);

		if (registered) {
			return;
		}
		registered = true;

		ServerTickEvents.END_SERVER_TICK.register(new ServerTickEvents.EndTick() {
			private int ticks;

			@Override
			public void onEndTick(MinecraftServer server) {
				int interval = tickInterval;
				Set<Holder<MobEffect>> currentBanned = banned;
				if (interval <= 0 || currentBanned.isEmpty()) {
					return;
				}

				ticks++;
				if (ticks < interval) {
					return;
				}
				ticks = 0;

				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					for (Holder<MobEffect> effect : currentBanned) {
						if (player.hasEffect(effect)) {
							player.removeEffect(effect);
						}
					}
				}
			}
		});
	}

	public static void reload(SmpCoreConfig config) {
		banned = resolveBannedEffects(config.effects.bannedEffects);
		tickInterval = config.effects.scanSeconds <= 0 ? -1 : Math.max(1, config.effects.scanSeconds * 20);
	}

	private static Set<Holder<MobEffect>> resolveBannedEffects(Iterable<String> ids) {
		Set<Holder<MobEffect>> banned = new HashSet<>();
		for (String raw : ids) {
			Identifier id = IdUtil.parse(raw);
			if (id == null) {
				continue;
			}
			BuiltInRegistries.MOB_EFFECT.get(id).ifPresent(banned::add);
		}
		return banned;
	}
}
