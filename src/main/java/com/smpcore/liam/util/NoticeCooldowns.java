package com.smpcore.liam.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NoticeCooldowns {
	private final Map<UUID, Long> lastNoticeMillisByPlayer = new HashMap<>();

	public boolean shouldNotify(UUID playerId, long minMillisBetweenNotices) {
		long now = System.currentTimeMillis();
		Long last = lastNoticeMillisByPlayer.get(playerId);
		if (last != null && now - last < minMillisBetweenNotices) {
			return false;
		}
		lastNoticeMillisByPlayer.put(playerId, now);
		return true;
	}
}

