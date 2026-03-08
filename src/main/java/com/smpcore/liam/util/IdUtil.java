package com.smpcore.liam.util;

import net.minecraft.resources.Identifier;

public final class IdUtil {
	private IdUtil() {
	}

	public static Identifier parse(String raw) {
		try {
			return Identifier.tryParse(raw);
		} catch (RuntimeException ignored) {
			return null;
		}
	}
}
