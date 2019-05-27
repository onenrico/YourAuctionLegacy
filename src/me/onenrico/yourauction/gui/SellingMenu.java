//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.text.StrLookup;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.onenrico.yourauction.database.Datamanager;
import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.main.YourAuctionAPI;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.menu.Action;
import me.onenrico.yourauction.menu.GUIMenu;
import me.onenrico.yourauction.menu.MenuItem;
import me.onenrico.yourauction.menu.MiningAnimation;
import me.onenrico.yourauction.menu.MyItem;
import me.onenrico.yourauction.object.EExpiredItem;
import me.onenrico.yourauction.object.ESaleItem;
import me.onenrico.yourauction.utils.EconomyUT;
import me.onenrico.yourauction.utils.ItemUT;
import me.onenrico.yourauction.utils.MathUT;
import me.onenrico.yourauction.utils.MessageUT;

public class SellingMenu extends GUIMenu {
	private static MyItem BorderItem;
	private static MyItem PrevPageItem;
	private static MyItem NextPageItem;
	private static MyItem RefreshItem;
	private static MyItem SellItem;
	private static MyItem WhatIsItem;
	private static MyItem ConfirmCancelItem;
	private static MyItem InventoryFullItem;
	private static MyItem BackItem;
	private static String title;
	public static Boolean first;
	private static HashMap<UUID, SellingMenu> cache;

	static {
		SellingMenu.first = true;
		SellingMenu.cache = new HashMap<>();
	}

	public static SellingMenu setup(final UUID uuid) {
		final SellingMenu mm = SellingMenu.cache.getOrDefault(uuid, new SellingMenu());
		if (mm.getName() == null) {
			mm.setName("SellingMenu");
			SellingMenu.cache.put(uuid, mm);
		}
		if (SellingMenu.first) {
			SellingMenu.BorderItem = GUIMenu.setupItem(mm, "BorderItem", -1);
			SellingMenu.PrevPageItem = GUIMenu.setupItem(mm, "PrevPageItem", 48);
			SellingMenu.NextPageItem = GUIMenu.setupItem(mm, "NextPageItem", 50);
			SellingMenu.RefreshItem = GUIMenu.setupItem(mm, "RefreshItem", 49);
			SellingMenu.SellItem = GUIMenu.setupItem(mm, "SellItem", -1);
			SellingMenu.WhatIsItem = GUIMenu.setupItem(mm, "WhatIsItem", 53);
			SellingMenu.ConfirmCancelItem = GUIMenu.setupItem(mm, "ConfirmCancelItem", -1);
			SellingMenu.InventoryFullItem = GUIMenu.setupItem(mm, "InventoryFullItem", -1);
			SellingMenu.BackItem = GUIMenu.setupItem(mm, "BackItem", 36);
			SellingMenu.title = Core.guiconfig.getStr(String.valueOf(mm.getName()) + ".Title", "&cNot Defined");
			SellingMenu.first = false;
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

	public static void open(final Player p, final OfflinePlayer target, int page) {
		MessageUT.plmessage(p, Locales.getValue("selling_menu"));
		final SellingMenu mm = setup(p.getUniqueId());
		final PlaceholderManager pm = new PlaceholderManager(Locales.pm);
		final List<ESaleItem> sorted = YourAuctionAPI.getSaleItems(target.getUniqueId());
		final int alldata = sorted.size();
		final int[] border = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 18, 27, 36, 45, 46, 47, 48, 49, 50, 51, 52, 53, 44, 35, 26,
				17 };
		final Integer[] temp = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37,
				38, 39, 40, 41, 42, 43 };
		List<Integer> itemslot = Arrays.asList(temp);
		int maxpage;

		if (alldata == 0) {
			maxpage = 1;
		} else if (alldata % itemslot.size() == 0) {
			maxpage = alldata / itemslot.size();
		} else {
			maxpage = alldata / itemslot.size() + 1;
		}
		if (page > maxpage) {
			page = maxpage;
		}
		if (page <= 0) {
			page = 1;
		}
		final int cpage = page;
		pm.add("selling", ESaleItem.table.getOwned(p.getUniqueId()).size());
		pm.add("page", new StringBuilder().append(page).toString());
		pm.add("maxpage", new StringBuilder().append(maxpage).toString());
		pm.add("nextpage", new StringBuilder().append(page + 1).toString());
		pm.add("prevpage", new StringBuilder().append(page - 1).toString());
		mm.setTitle(pm.process(SellingMenu.title));
		mm.setRow(6);
		mm.build();
		int[] array;
		for (int length = (array = border).length, j = 0; j < length; ++j) {
			final int slot = array[j];
			mm.setItem(slot, SellingMenu.BorderItem);
		}
		mm.setItem(SellingMenu.RefreshItem.slot, pm.process(SellingMenu.RefreshItem.item)).addAction(new Action() {
			@Override
			public void act() {
				SellingMenu.open(p, target, cpage);
			}
		});
		mm.setItem(SellingMenu.WhatIsItem.slot, pm.process(SellingMenu.WhatIsItem.item));
		mm.setItem(SellingMenu.BackItem.slot, pm.process(SellingMenu.BackItem.item)).addAction(new Action() {
			@Override
			public void act() {
				MainMenu.open(p, target, 1);
			}
		});
		if (page + 1 <= maxpage) {
			mm.setItem(SellingMenu.NextPageItem.slot, pm.process(SellingMenu.NextPageItem.clone())).addAction(new Action() {
				@Override
				public void act() {
					SellingMenu.open(p, target, cpage + 1);
				}
			});
		}
		if (page - 1 > 0) {
			mm.setItem(SellingMenu.PrevPageItem.slot, pm.process(SellingMenu.PrevPageItem.clone())).addAction(new Action() {
				@Override
				public void act() {
					SellingMenu.open(p, target, cpage - 1);
				}
			});
		}
		int offset = itemslot.size() * (page - 1);
		if (offset < 0) {
			offset = 0;
		}
		final int available = alldata - offset;
		if (available < 7) {
			itemslot = Arrays.asList(21, 22, 23, 30, 31, 32);
		} else if (available < 11) {
			itemslot = Arrays.asList(20, 21, 22, 23, 24, 29, 30, 31, 32, 33);
		} else if (available < 21) {
			itemslot = Arrays.asList(11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42);
		}
		int index = 0;
		for (int i = 0; i < available && i + 1 <= itemslot.size(); ++i) {
			final ESaleItem es = sorted.get(i + offset);
			place(mm, p, target, es, itemslot.get(index++), page);
		}
		mm.open(p);
	}

	public static void place(final SellingMenu mm, final Player p, final OfflinePlayer target, final ESaleItem es,
			final int slot, final int page) {
		final PlaceholderManager pm = es.getPlaceholder();
		pm.add("nextpage", new StringBuilder().append(page + 1).toString());
		pm.add("prevpage", new StringBuilder().append(page - 1).toString());
		pm.add("stack", "64");
		pm.add("money", new StrLookup() {
			@Override
			public String lookup(String msg) {
				return MathUT.format(EconomyUT.getRawBal(p));
			}
		});
		final ItemStack icon = SellingMenu.SellItem.clone();
		final ItemStack av = es.getItem().clone();
		ItemUT.changeDisplayName(av, ItemUT.getName(icon));
		List<String> lore = ItemUT.getLore(av);
		if (lore == null) {
			lore = new ArrayList<>();
		}
		lore.addAll(ItemUT.getLore(icon));
		ItemUT.changeLore(av, lore);
		final ItemStack bv = SellingMenu.ConfirmCancelItem.clone();
		final ItemStack cacheav = av;
		mm.setItem(slot, cacheav, pm).addAction(new Action() {
			@Override
			public void act() {
				if (!ESaleItem.table.getLoadedData().contains(es)) {
					SellingMenu.open(p, target, page);
					return;
				}
				if (es.isExpired()) {
					EExpiredItem.create(es);
					SellingMenu.open(p, target, page);
					return;
				}
				final MenuItem cache = mm.getMenuItem(slot).clone();
				if (!Core.canceling.isAllow(p.getUniqueId())) {
					if (p.getInventory().firstEmpty() != -1) {
						Core.canceling.add(p.getUniqueId());
						mm.setItem(slot, pm.process(bv)).addAction(new Action() {
							@Override
							public void act() {
								if (!ESaleItem.table.getLoadedData().contains(es)) {
									SellingMenu.open(p, target, page);
									Core.canceling.remove(p.getUniqueId());
									return;
								}
								Core.canceling.remove(p.getUniqueId());
								cache.setSlot(-1);
								p.getInventory().addItem(new ItemStack[] { es.getItem().clone() });
								Datamanager.delete(es);
								SellingMenu.open(p, target, page);
								if ((pm.get("itemname").equals(""))) {
									pm.add("itemname", es.getItem().getType().toString());
								}
								MessageUT.plmessage(p, Locales.getValue("success_cancelled"), pm);
							}
						});
						new BukkitRunnable() {
							@Override
							public void run() {
								if (cache.getSlot() < 0) {
									return;
								}
								Core.canceling.remove(p.getUniqueId());
								mm.setItem(slot, cache);
							}
						}.runTaskLater(Core.getThis(), 40L);
					} else {
						mm.setItem(slot, pm.process(SellingMenu.InventoryFullItem.item)).addAction(new Action() {
							@Override
							public void act() {
								MessageUT.plmessage(p, "<sound>BLOCK_NOTE_PLING");
							}
						});
						new BukkitRunnable() {
							@Override
							public void run() {
								mm.setItem(slot, cache);
							}
						}.runTaskLater(Core.getThis(), 40L);
					}
				} else {
					MessageUT.plmessage(p, "<sound>BLOCK_NOTE_PLING");
				}
			}
		});
	}
}
