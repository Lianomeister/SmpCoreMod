package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.IdUtil;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.HashSet;
import java.util.Set;

import static net.minecraft.core.component.DataComponents.POTION_CONTENTS;

public final class PotionBanFeature {
	private static boolean registered;

	private static volatile boolean banAll;
	private static volatile Set<Holder<MobEffect>> bannedEffects = Set.of();

	private PotionBanFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);
		if (registered) {
			return;
		}
		registered = true;

		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (!banAll && bannedEffects.isEmpty()) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);
			if (!isPotionItem(stack)) {
				return InteractionResult.PASS;
			}

			if (banAll) {
				return InteractionResult.FAIL;
			}

			PotionContents contents = stack.getOrDefault(POTION_CONTENTS, PotionContents.EMPTY);
			if (contents == PotionContents.EMPTY) {
				return InteractionResult.PASS;
			}

			for (var effectInstance : contents.getAllEffects()) {
				Holder<MobEffect> effect = effectInstance.getEffect();
				if (bannedEffects.contains(effect)) {
					return InteractionResult.FAIL;
				}
			}

			return InteractionResult.PASS;
		});
	}

	public static void reload(SmpCoreConfig config) {
		banAll = config.potions.banAll;
		bannedEffects = resolveEffects(config.potions.bannedPotionEffects);
	}

	private static boolean isPotionItem(ItemStack stack) {
		return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
	}

	private static Set<Holder<MobEffect>> resolveEffects(Iterable<String> ids) {
		Set<Holder<MobEffect>> set = new HashSet<>();
		for (String raw : ids) {
			Identifier id = IdUtil.parse(raw);
			if (id == null) {
				continue;
			}
			BuiltInRegistries.MOB_EFFECT.get(id).ifPresent(set::add);
		}
		return set;
	}
}

