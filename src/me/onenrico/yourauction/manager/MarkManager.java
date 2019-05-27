//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.manager;

import java.util.HashMap;
import java.util.UUID;

public class MarkManager {
	public HashMap<UUID, UUID> mark_data;

	public MarkManager() {
		mark_data = new HashMap<>();
	}

	public void add(final UUID p, final UUID target) {
		mark_data.put(p, target);
	}

	public void remove(final UUID p) {
		mark_data.remove(p);
	}

	public UUID getTarget(final UUID p) {
		return mark_data.getOrDefault(p, null);
	}

	public Boolean hasTarget(final UUID p) {
		return mark_data.containsKey(p);
	}
}
