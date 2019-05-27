package me.onenrico.yourauction.menu;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.utils.ItemUT;

public class MiningAnimation extends OpenAnimation {
	@Override
	public void open(final Runnable callback, final GUIMenu gm, final Player p) {
		if (p.isOnline()) {
			new BukkitRunnable() {
				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					final Inventory inv = gm.getInventory();
					
					final HashMap<Integer, Set<Integer>> rows = new HashMap<>();
					for (final MenuItem mi : gm.getMenuitems()) {
						if (mi.getItem().getType().equals(Material.AIR)) {
							continue;
						}
						final int crow = mi.getSlot() / 9 + 1;
						final Set<Integer> inside = rows.getOrDefault(crow, new HashSet<Integer>());
						if (inside.isEmpty()) {
							inside.add(mi.getSlot());
							rows.put(crow, inside);
						} else {
							inside.add(mi.getSlot());
						}
						ItemStack material = null;
						if (ConfigManager.blocks_animation.size() < crow) {
							material = ConfigManager.blocks_animation.get(ConfigManager.blocks_animation.size() - 1);
						} else {
							material = ConfigManager.blocks_animation.get(crow - 1);
						}
						ItemStack item = ItemUT.createItem(material.getType(), "&r", material.getDurability());
						if (mi.getSlot() > -1) {
							inv.setItem(mi.getSlot(), gm.secure(item, true));
						}
					}
					p.openInventory(inv);
					new BukkitRunnable() {
						int coffset = -1;
						boolean finish = true;
						final int row = gm.getInv().getSize() / 9;

						@Override
						public void run() {
							if (!inv.getViewers().contains(p)) {
								cancel();
								return;
							}
							finish = true;
							++coffset;
							for (int r = 0; r < row; ++r) {
								final int slot = coffset + 9 * r - r;
								final int prevslot = slot - 1;
								boolean there = false;
								if (prevslot >= 9 * r) {
									if (slot > 9 + 9 * r) {
										continue;
									}
									if (prevslot > 53) {
										continue;
									}
									for (final int gmslot : rows.getOrDefault(r + 1, new HashSet<Integer>())) {
										if (gmslot == prevslot) {
											final MenuItem mi = gm.getMenuItem(gmslot);
											final PlaceholderManager pm = mi.getPm();
											if (pm != null) {
												inv.setItem(prevslot, pm.process(mi.getItem().clone()));
											} else {
												inv.setItem(prevslot, mi.getItem().clone());
											}
											there = true;
										}
									}
									if (!there) {
										inv.setItem(prevslot, ItemUT.createItem(Material.AIR));
									}
								}
								if (slot >= 9 * r) {
									if (slot == 9 + 9 * r && !there) {
										inv.setItem(prevslot, ItemUT.createItem(Material.AIR));
									}
									if (slot <= 8 + 9 * r) {
										finish = false;
										ItemStack material = null;
										if (ConfigManager.tools_animation.size() - 1 < r) {
											material = ConfigManager.tools_animation
													.get(ConfigManager.tools_animation.size() - 1);
										} else {
											material = ConfigManager.tools_animation.get(r);
										}
										ItemStack item = ItemUT.createItem(material.getType(), "&r",
												material.getDurability());
										inv.setItem(slot, gm.secure(item, true));
									}
								}
							}
							if (finish) {
								cancel();
								if (callback != null) {
									callback.run();
								}
							}
						}
					}.runTaskTimer(Core.getThis(), 0L, 1L);
				}
			}.runTask(Core.getThis());
		}
	}
}
