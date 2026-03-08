package com.smpcore.liam.item;

import com.smpcore.liam.SmpCore;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class SmpCoreItems {
	public static final Item WARDEN_HEART = new Item(new Item.Properties().rarity(Rarity.RARE));

	private static boolean registered;

	private SmpCoreItems() {
	}

	public static void registerAll() {
		if (registered) {
			return;
		}
		registered = true;

		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(SmpCore.MOD_ID, "warden_heart"), WARDEN_HEART);
	}
}

