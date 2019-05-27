//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.manager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ToggleManager {
	public Set<UUID> toggle_data;

	public ToggleManager() {
		toggle_data = new HashSet<>();
	}

	public void add(final UUID p) {
		toggle_data.add(p);
	}

	public void remove(final UUID p) {
		toggle_data.remove(p);
	}

	public boolean isAllow(final UUID p) {
		return toggle_data.contains(p);
	}
}
