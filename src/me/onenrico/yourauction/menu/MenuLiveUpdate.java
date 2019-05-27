//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.menu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.utils.ItemUT;

public class MenuLiveUpdate {
	private static Set<GUIMenu> animated;

	static {
		MenuLiveUpdate.animated = new HashSet<>();
	}

	public static void addAnimated(final GUIMenu gm) {
		MenuLiveUpdate.animated.add(gm);
	}

	public static void removeAnimated(final GUIMenu gm) {
		if (MenuLiveUpdate.animated.contains(gm)) {
			MenuLiveUpdate.animated.remove(gm);
		}
	}

	public static void refresh(final GUIMenu gm) {
		if (gm.getInv().getViewers().isEmpty()) {
			MenuLiveUpdate.animated.remove(gm);
			return;
		}
		for (final MenuItem mi : gm.getMenuitems()) {
			refresh(gm, mi);
		}
	}

	public static void refresh(final GUIMenu gm, final MenuItem mi) {
		final PlaceholderManager pm = mi.getPm();
		if (pm != null) {
			if (mi.getSlot() > -1) {
				final ItemStack it = gm.getInv().getItem(mi.getSlot());
				if (it != null && !it.getType().equals(Material.AIR)) {
					ItemUT.changeDisplayName(it, pm.process(ItemUT.getName(mi.getItem())));
					ItemUT.changeLore(it, pm.process(ItemUT.getLore(mi.getItem())));
				}
			}
		}
	}

	public static void startTimer() {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (final GUIMenu gm : new ArrayList<>(MenuLiveUpdate.animated)) {
					MenuLiveUpdate.refresh(gm);
				}
			}
		}.runTaskTimer(Core.getThis(), 20L, 20L);
	}
}
