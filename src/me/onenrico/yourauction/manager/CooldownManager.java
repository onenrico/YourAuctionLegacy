//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.manager;

import java.util.HashMap;
import java.util.UUID;

import me.onenrico.yourauction.utils.TimeManager;

public class CooldownManager {
	private HashMap<UUID, Long> present_data;
	private HashMap<UUID, Long> duration_data;

	public CooldownManager() {
		present_data = new HashMap<>();
		duration_data = new HashMap<>();
	}

	public void add(final UUID p, final Long duration) {
		present_data.put(p, System.currentTimeMillis());
		duration_data.put(p, duration);
	}

	public void remove(final UUID p) {
		present_data.remove(p);
		duration_data.remove(p);
	}

	public long getDif(final UUID p) {
		return (present_data.getOrDefault(p, 0L) == 0L) ? 0L : (System.currentTimeMillis() - present_data.get(p));
	}

	public long getRemain(final UUID p) {
		return getDur(p) - getDif(p);
	}

	public long getDur(final UUID p) {
		return (duration_data.getOrDefault(p, 0L) == 0L) ? 0L : duration_data.get(p);
	}

	public boolean isAllow(final UUID p) {
		if (getDif(p) >= getDur(p)) {
			remove(p);
			return true;
		}
		return false;
	}

	public String getDifFormatted(final UUID p) {
		return TimeManager.formatTime(getDif(p));
	}

	public String getRemainFormatted(final UUID p) {
		return TimeManager.formatTime((getDur(p) - getDif(p)) / 1000L);
	}
}
