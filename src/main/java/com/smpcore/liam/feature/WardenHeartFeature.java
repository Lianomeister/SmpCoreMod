package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.item.SmpCoreItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

public final class WardenHeartFeature {
	private static boolean registered;
	private static volatile boolean enabled;

	private WardenHeartFeature() {
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
			if (entity.getType() != EntityType.WARDEN) {
				return;
			}
			if (entity.level() instanceof ServerLevel serverLevel) {
				entity.spawnAtLocation(serverLevel, new ItemStack(SmpCoreItems.WARDEN_HEART));
			}
		});
	}

	public static void reload(SmpCoreConfig config) {
		enabled = config.gameplay.wardenHeartDrop;
	}
}
