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
import me.onenrico.yourauction.utils.EconomyUT;
import me.onenrico.yourauction.utils.ItemUT;
import me.onenrico.yourauction.utils.MathUT;
import me.onenrico.yourauction.utils.MessageUT;

public class ExpiredMenu extends GUIMenu {
	private static MyItem BorderItem;
	private static MyItem PrevPageItem;
	private static MyItem NextPageItem;
	private static MyItem RefreshItem;
	private static MyItem ExpiredItem;
	private static MyItem WhatIsItem;
	private static MyItem InventoryFullItem;
	private static MyItem BackItem;
	private static MyItem TakeAllItem;
	private static String title;
	public static Boolean first;
	private static HashMap<UUID, ExpiredMenu> cache;

	static {
		ExpiredMenu.first = true;
		ExpiredMenu.cache = new HashMap<>();
	}

	public static ExpiredMenu setup(final UUID uuid) {
		final ExpiredMenu mm = ExpiredMenu.cache.getOrDefault(uuid, new ExpiredMenu());
		if (mm.getName() == null) {
			mm.setName("ExpiredMenu");
			ExpiredMenu.cache.put(uuid, mm);
		}
		if (ExpiredMenu.first) {
			ExpiredMenu.BorderItem = GUIMenu.setupItem(mm, "BorderItem", -1);
			ExpiredMenu.PrevPageItem = GUIMenu.setupItem(mm, "PrevPageItem", 48);
			ExpiredMenu.NextPageItem = GUIMenu.setupItem(mm, "NextPageItem", 50);
			ExpiredMenu.RefreshItem = GUIMenu.setupItem(mm, "RefreshItem", 49);
			ExpiredMenu.ExpiredItem = GUIMenu.setupItem(mm, "ExpiredItem", -1);
			ExpiredMenu.WhatIsItem = GUIMenu.setupItem(mm, "WhatIsItem", 53);
			ExpiredMenu.TakeAllItem = GUIMenu.setupItem(mm, "TakeAllItem", 4);
			ExpiredMenu.InventoryFullItem = GUIMenu.setupItem(mm, "InventoryFullItem", -1);
			ExpiredMenu.BackItem = GUIMenu.setupItem(mm, "BackItem", 27);
			ExpiredMenu.title = Core.guiconfig.getStr(String.valueOf(mm.getName()) + ".Title", "&cNot Defined");
			ExpiredMenu.first = false;
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
		MessageUT.plmessage(p, Locales.getValue("expired_menu"));
		final ExpiredMenu mm = setup(p.getUniqueId());
		final PlaceholderManager pm = new PlaceholderManager(Locales.pm);
		final List<EExpiredItem> sorted = YourAuctionAPI.getExpiredItems(target.getUniqueId());
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
		pm.add("amount", EExpiredItem.table.getOwned(p.getUniqueId()).size());
		pm.add("page", new StringBuilder().append(page).toString());
		pm.add("maxpage", new StringBuilder().append(maxpage).toString());
		pm.add("nextpage", new StringBuilder().append(page + 1).toString());
		pm.add("prevpage", new StringBuilder().append(page - 1).toString());
		mm.setTitle(pm.process(ExpiredMenu.title));
		mm.setRow(6);
		mm.build();
		int[] array;
		for (int length = (array = border).length, j = 0; j < length; ++j) {
			final int slot = array[j];
			mm.setItem(slot, ExpiredMenu.BorderItem);
		}
		mm.setItem(ExpiredMenu.RefreshItem.slot, pm.process(ExpiredMenu.RefreshItem.item)).addAction(new Action() {
			@Override
			public void act() {
				ExpiredMenu.open(p, target, cpage);
			}
		});
		mm.setItem(ExpiredMenu.WhatIsItem.slot, pm.process(ExpiredMenu.WhatIsItem.item));
		mm.setItem(ExpiredMenu.BackItem.slot, pm.process(ExpiredMenu.BackItem.item)).addAction(new Action() {
			@Override
			public void act() {
				MainMenu.open(p, target, 1);
			}
		});
		if (page + 1 <= maxpage) {
			mm.setItem(ExpiredMenu.NextPageItem.slot, pm.process(ExpiredMenu.NextPageItem.clone())).addAction(new Action() {
				@Override
				public void act() {
					ExpiredMenu.open(p, target, cpage + 1);
				}
			});
		}
		if (page - 1 > 0) {
			mm.setItem(ExpiredMenu.PrevPageItem.slot, pm.process(ExpiredMenu.PrevPageItem.clone())).addAction(new Action() {
				@Override
				public void act() {
					ExpiredMenu.open(p, target, cpage - 1);
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
			final EExpiredItem es = sorted.get(i + offset);
			place(mm, p, target, es, itemslot.get(index++), page);
		}
		if (alldata > 0) {
			mm.setItem(TakeAllItem).addAction(new Action() {
				@Override
				public void act() {
					PlaceholderManager espm;
					for (EExpiredItem es : sorted) {
						espm = es.getPlaceholder();
						if (!EExpiredItem.table.getLoadedData().contains(es)) {
							continue;
						}
						if (es.isExpired()) {
							Datamanager.delete(es);
							continue;
						}
						if (p.getInventory().firstEmpty() != -1) {
							p.getInventory().addItem(es.getItem());
							Datamanager.delete(es);
							if ((espm.get("itemname").equals(""))) {
								espm.add("itemname", es.getItem().getType().toString());
							}
							MessageUT.plmessage(p, Locales.getValue("success_take_expired"), espm);
						} else {
							continue;
						}
					}
					ExpiredMenu.open(p, target, 1);
				}
			});
		}
		mm.open(p);
	}

	public static void place(final ExpiredMenu mm, final Player p, final OfflinePlayer target, final EExpiredItem es,
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
		final ItemStack icon = ExpiredMenu.ExpiredItem.clone();
		ItemStack av = es.getItem().clone();
		ItemUT.changeDisplayName(av, ItemUT.getName(icon));
		List<String> lore = ItemUT.getLore(av);
		if (lore == null) {
			lore = new ArrayList<>();
		}
		lore.addAll(ItemUT.getLore(icon));
		ItemUT.changeLore(av, lore);
		final ItemStack cacheav = av.clone();
		mm.setItem(slot, cacheav, pm).addAction(new Action() {
			@Override
			public void act() {
				if (!EExpiredItem.table.getLoadedData().contains(es)) {
					ExpiredMenu.open(p, target, page);
					return;
				}
				if (es.isExpired()) {
					Datamanager.delete(es);
					ExpiredMenu.open(p, target, page);
					return;
				}
				final MenuItem cache = mm.getMenuItem(slot).clone();
				if (p.getInventory().firstEmpty() != -1) {
					cache.setSlot(-1);
					p.getInventory().addItem(new ItemStack[] { es.getItem() });
					Datamanager.delete(es);
					ExpiredMenu.open(p, target, page);
					if ((pm.get("itemname").equals(""))) {
						pm.add("itemname", es.getItem().getType().toString());
					}
					MessageUT.plmessage(p, Locales.getValue("success_take_expired"), pm);
				} else {
					mm.setItem(slot, pm.process(ExpiredMenu.InventoryFullItem.item)).addAction(new Action() {
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
			}
		});
	}
}
