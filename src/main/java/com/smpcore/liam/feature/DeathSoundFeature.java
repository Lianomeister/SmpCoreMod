package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.IdUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public final class DeathSoundFeature {
	private static boolean registered;

	private static volatile boolean enabled;
	private static volatile Identifier soundId;
	private static volatile float volume;
	private static volatile float pitch;

	private DeathSoundFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
		if (registered) {
			return;
		}
		registered = true;

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (!enabled) {
				return;
			}
			if (!(entity instanceof ServerPlayer player)) {
				return;
			}

			Identifier id = soundId;
			if (id == null) {
				return;
			}
			SoundEvent event = BuiltInRegistries.SOUND_EVENT.getValue(id);
			if (event == null) {
				return;
			}

			ServerLevel level = (ServerLevel) player.level();
			level.playSound(
					null,
					player.getX(),
					player.getY(),
					player.getZ(),
					event,
					SoundSource.PLAYERS,
					Math.max(0.0f, volume),
					Math.max(0.0f, pitch)
			);
		});
	}

	public static void reload(SmpCoreConfig config) {
		enabled = config.death.customDeathSoundEnabled;
		soundId = IdUtil.parse(config.death.customDeathSoundId);
		volume = (float) config.death.customDeathSoundVolume;
		pitch = (float) config.death.customDeathSoundPitch;
	}
}
