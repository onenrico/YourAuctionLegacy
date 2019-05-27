//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.gui;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.menu.Action;
import me.onenrico.yourauction.menu.GUIMenu;
import me.onenrico.yourauction.menu.MiningAnimation;
import me.onenrico.yourauction.menu.MyItem;
import me.onenrico.yourauction.utils.EMaterial;
import me.onenrico.yourauction.utils.EconomyUT;
import me.onenrico.yourauction.utils.MessageUT;

public class SellallMenu extends GUIMenu {
	private static MyItem BorderItem;
	private static MyItem SellAllItem;
	private static MyItem FilledItem;
	private static MyItem ListItem;
	private static MyItem ZeroItem;
	private static MyItem EmptyItem;
	private static MyItem WhatIsItem;
	private static MyItem BackItem;
	private static String title;
	public static Boolean first;
	private static HashMap<UUID, SellallMenu> cache;

	static {
		SellallMenu.first = true;
		SellallMenu.cache = new HashMap<>();
	}
	final public static Integer[] allslot = { 10, 11, 12, 13, 14, 15, 16, 28, 29, 30, 31, 32, 33, 34 };
	final public static Integer[] allrewardslot = { 19, 20, 21, 22, 23, 24, 25, 37, 38, 39, 40, 41, 42, 43 };

	boolean animationdone = false;

	// 00 01 02 03 04 05 06 07 08
	// 09 10 11 12 13 14 15 16 17
	// 18 19 20 21 22 23 24 25 26
	// 27 28 29 30 31 32 33 34 35
	// 36 37 38 39 40 41 42 43 44
	// 45 46 47 48 49 50 51 52 53
	public static SellallMenu setup(final UUID uuid) {
		final SellallMenu mm = SellallMenu.cache.getOrDefault(uuid, new SellallMenu());
		if (mm.getName() == null) {
			mm.setName("SellAllMenu");
			SellallMenu.cache.put(uuid, mm);
		}
		if (SellallMenu.first) {
			SellallMenu.ListItem = GUIMenu.setupItem(mm, "ListItem", 4);
			SellallMenu.BorderItem = GUIMenu.setupItem(mm, "BorderItem", -1);
			SellallMenu.SellAllItem = GUIMenu.setupItem(mm, "SellAllItem", 49);
			SellallMenu.FilledItem = GUIMenu.setupItem(mm, "FilledItem", -1);
			SellallMenu.ZeroItem = GUIMenu.setupItem(mm, "ZeroItem", -1);
			SellallMenu.EmptyItem = GUIMenu.setupItem(mm, "EmptyItem", -1);
			SellallMenu.WhatIsItem = GUIMenu.setupItem(mm, "WhatIsItem", 53);
			SellallMenu.BackItem = GUIMenu.setupItem(mm, "BackItem", 9);
			SellallMenu.title = Core.guiconfig.getStr(String.valueOf(mm.getName()) + ".Title", "&cNot Defined");
			SellallMenu.first = false;
		}
		if (ConfigManager.animation_enabled) {
			mm.setAnimation(new MiningAnimation());
		}
		mm.getMenuitems().clear();
		if (mm.getInv() != null) {
			mm.getInv().clear();
		}
		return mm;
	}

	public double total;

	public void check(final Player p) {
		Inventory top = p.getOpenInventory().getTopInventory();
		if (top != null && top.getHolder() instanceof SellallMenu) {
			if (!animationdone) {
				total = 0;
				return;
			}
			new BukkitRunnable() {
				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					final PlaceholderManager pm = new PlaceholderManager(Locales.pm);
					SellallMenu mm = (SellallMenu) top.getHolder();
					total = 0;
					for (int slot : allslot) {
						ItemStack i = mm.getInv().getItem(slot);
						if (i != null && !i.getType().equals(Material.AIR)) {
							EMaterial current = EMaterial.fromMaterial(i.getType());
							double price = ConfigManager.price_data.getOrDefault(current, 0d) * i.getAmount();
							if (!current.isUnique(current)) {
								if (i.getDurability() > 0) {
									if (!ConfigManager.allowdamaged2) {
										price = 0;
									}
								}
							} else {
								String search = current.m + ":" + i.getDurability();
								current = EMaterial.fromString(search);
								price = ConfigManager.price_data.getOrDefault(current, 0d) * i.getAmount();
							}
							pm.add("price", price);
							if (price > 0) {
								mm.setItem(slot + 9, pm.process(FilledItem.clone()));
							} else {
								mm.setItem(slot + 9, pm.process(ZeroItem.clone()));
							}
							total += price;
							continue;
						}
						pm.add("price", 0);
						mm.setItem(slot + 9, EmptyItem);
					}
					DecimalFormat df = new DecimalFormat(".##");
					if (ConfigManager.tax2 > 0) {
						double taxcut = total - (total * ConfigManager.tax2 / 100);
						double newtotal = total - taxcut;
						pm.add("total", df.format(total) + " - " + taxcut + " = " + df.format(newtotal));
						total = newtotal;
					} else {
						pm.add("total", df.format(total));
					}
					mm.setItem(SellallMenu.SellAllItem.slot, pm.process(SellallMenu.SellAllItem.clone()), false);
					p.updateInventory();
				}
			}.runTask(Core.getThis());
		}
	}

	public void sell(final Player p) {
		boolean empty = true;
		for (int slot : allslot) {
			ItemStack i = p.getOpenInventory().getTopInventory().getItem(slot);
			if (i != null && !i.getType().equals(Material.AIR)) {
				empty = false;
				break;
			}
		}
		if (!empty) {
			final PlaceholderManager pm = new PlaceholderManager(Locales.pm);
			pm.add("total", total);
			MessageUT.plmessage(p, Locales.getValue("sell_all"), pm);
			EconomyUT.addBal(p, total);
			SellallMenu.open(p, p);
		}
	}

	public static void open(final Player p, final OfflinePlayer target) {
		MessageUT.plmessage(p, Locales.getValue("sellall_menu"));
		final SellallMenu mm = setup(p.getUniqueId());
		mm.animationdone = false;
		mm.setEditable(true);
		mm.setStealable(true);
		final PlaceholderManager pm = new PlaceholderManager(Locales.pm);
		final int[] border = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 18, 27, 36, 45, 46, 47, 48, 49, 50, 51, 52, 53, 44, 35, 26,
				17 };
		mm.setTitle(pm.process(SellallMenu.title));
		mm.setRow(6);
		mm.build();
		int[] array;
		for (int length = (array = border).length, j = 0; j < length; ++j) {
			final int slot = array[j];
			mm.setItem(slot, SellallMenu.BorderItem);
		}
		pm.add("total", 0);
		mm.setItem(SellallMenu.SellAllItem.slot, pm.process(SellallMenu.SellAllItem.clone())).addAction(new Action() {
			@Override
			public void act() {
				mm.sell(p);
			}
		});
		mm.setItem(SellallMenu.WhatIsItem.slot, pm.process(SellallMenu.WhatIsItem.item));
		if (ConfigManager.showback) {
			mm.setItem(SellallMenu.BackItem.slot, pm.process(SellallMenu.BackItem.item)).addAction(new Action() {
				@Override
				public void act() {
					MainMenu.open(p, target, 1);
				}
			});
		}
		mm.setItem(SellallMenu.ListItem.slot, pm.process(ListItem.item)).addAction(new Action() {

			@Override
			public void act() {
				SellAllInfo.open(p, target, 1);
			}
		});
		mm.open(new Runnable() {

			@Override
			public void run() {
				mm.animationdone = true;
				mm.check(p);
			}
		}, p);
		mm.setCloseaction(new Action() {
			@Override
			public void act() {
				for (int slot : allslot) {
					ItemStack i = mm.getInv().getItem(slot);
					if (i != null && !i.getType().equals(Material.AIR)) {
						Item it = p.getWorld().dropItem(p.getLocation(), i);
						it.setVelocity(new Vector(0, 0.1, 0));
						it.setPickupDelay(0);
					}
				}
			}
		});
	}

}
