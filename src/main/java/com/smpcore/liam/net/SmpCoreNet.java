package com.smpcore.liam.net;

import com.smpcore.liam.SmpCore;
import net.minecraft.resources.Identifier;

public final class SmpCoreNet {
	public static final Identifier S2C_OPEN_ADMIN = Identifier.fromNamespaceAndPath(SmpCore.MOD_ID, "open_admin");
	public static final Identifier C2S_SAVE_CONFIG = Identifier.fromNamespaceAndPath(SmpCore.MOD_ID, "save_config");

	private SmpCoreNet() {
	}
}

