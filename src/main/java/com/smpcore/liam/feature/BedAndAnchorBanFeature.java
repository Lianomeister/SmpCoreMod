package com.smpcore.liam.feature;

import com.smpcore.liam.config.SmpCoreConfig;
import com.smpcore.liam.util.NoticeCooldowns;
import com.smpcore.liam.util.TextUtil;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class BedAndAnchorBanFeature {
	private static final NoticeCooldowns NOTICE_COOLDOWNS = new NoticeCooldowns();
	private static boolean registered;

	private static volatile boolean banBedBombing;
	private static volatile boolean banAnchorBombing;
	private static volatile boolean actionBar;
	private static volatile long minMillisBetweenNotices;

	private BedAndAnchorBanFeature() {
	}

	public static void init(SmpCoreConfig config) {
		reload(config);

		if (registered) {
			return;
		}
		registered = true;

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!(player instanceof ServerPlayer serverPlayer)) {
				return InteractionResult.PASS;
			}

			if (!banBedBombing && !banAnchorBombing) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hitResult.getBlockPos();
			BlockState state = world.getBlockState(pos);

			if (banBedBombing && state.getBlock() instanceof BedBlock) {
				if (!world.dimension().equals(Level.OVERWORLD)) {
					notify(serverPlayer, "Beds are disabled in this dimension.");
					return InteractionResult.FAIL;
				}
			}

			if (banAnchorBombing && state.is(Blocks.RESPAWN_ANCHOR)) {
				if (!world.dimension().equals(Level.NETHER)) {
					notify(serverPlayer, "Respawn anchors are disabled in this dimension.");
					return InteractionResult.FAIL;
				}
			}

			return InteractionResult.PASS;
		});
	}

	public static void reload(SmpCoreConfig config) {
		banBedBombing = config.bans.banBedBombing;
		banAnchorBombing = config.bans.banAnchorBombing;
		actionBar = config.messages.actionBar;
		minMillisBetweenNotices = config.messages.minMillisBetweenNotices;
	}

	private static void notify(ServerPlayer player, String message) {
		if (!NOTICE_COOLDOWNS.shouldNotify(player.getUUID(), minMillisBetweenNotices)) {
			return;
		}

		if (actionBar) {
			TextUtil.actionBar(player, message);
		} else {
			TextUtil.chat(player, message);
		}
	}
}
