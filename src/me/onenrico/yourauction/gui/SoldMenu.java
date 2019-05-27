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

import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.main.YourAuctionAPI;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.menu.Action;
import me.onenrico.yourauction.menu.GUIMenu;
import me.onenrico.yourauction.menu.MiningAnimation;
import me.onenrico.yourauction.menu.MyItem;
import me.onenrico.yourauction.object.EPlayer;
import me.onenrico.yourauction.object.ESoldItem;
import me.onenrico.yourauction.utils.EconomyUT;
import me.onenrico.yourauction.utils.ItemUT;
import me.onenrico.yourauction.utils.MathUT;
import me.onenrico.yourauction.utils.MessageUT;

public class SoldMenu extends GUIMenu {
	private static MyItem BorderItem;
	private static MyItem PrevPageItem;
	private static MyItem NextPageItem;
	private static MyItem RefreshItem;
	private static MyItem OnAutoCollectItem;
	private static MyItem OffAutoCollectItem;
	private static MyItem SoldItem;
	private static MyItem TakenItem;
	private static MyItem WhatIsItem;
	private static MyItem BackItem;
	private static String title;
	public static Boolean first;
	private static HashMap<UUID, SoldMenu> cache;

	static {
		SoldMenu.first = true;
		SoldMenu.cache = new HashMap<>();
	}

	public static SoldMenu setup(final UUID uuid) {
		final SoldMenu mm = SoldMenu.cache.getOrDefault(uuid, new SoldMenu());
		if (mm.getName() == null) {
			mm.setName("SoldMenu");
			SoldMenu.cache.put(uuid, mm);
		}
		if (SoldMenu.first) {
			SoldMenu.BorderItem = GUIMenu.setupItem(mm, "BorderItem", -1);
			SoldMenu.PrevPageItem = GUIMenu.setupItem(mm, "PrevPageItem", 48);
			SoldMenu.NextPageItem = GUIMenu.setupItem(mm, "NextPageItem", 50);
			SoldMenu.RefreshItem = GUIMenu.setupItem(mm, "RefreshItem", 49);
			SoldMenu.SoldItem = GUIMenu.setupItem(mm, "SoldItem", -1);
			SoldMenu.TakenItem = GUIMenu.setupItem(mm, "TakenItem", -1);
			SoldMenu.WhatIsItem = GUIMenu.setupItem(mm, "WhatIsItem", 53);
			SoldMenu.OnAutoCollectItem = GUIMenu.setupItem(mm, "OnAutoCollectItem", 4);
			SoldMenu.OffAutoCollectItem = GUIMenu.setupItem(mm, "OffAutoCollectItem", 4);
			SoldMenu.BackItem = GUIMenu.setupItem(mm, "BackItem", 18);
			SoldMenu.title = Core.guiconfig.getStr(String.valueOf(mm.getName()) + ".Title", "&cNot Defined");
			SoldMenu.first = false;
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
		MessageUT.plmessage(p, Locales.getValue("sold_menu"));
		final SoldMenu mm = setup(p.getUniqueId());
		final PlaceholderManager pm = new PlaceholderManager(Locales.pm);
		final List<ESoldItem> sorted = YourAuctionAPI.getSoldItems(target.getUniqueId());
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
		pm.add("amount", ESoldItem.table.getOwned(p.getUniqueId()).size());
		pm.add("page", new StringBuilder().append(page).toString());
		pm.add("maxpage", new StringBuilder().append(maxpage).toString());
		pm.add("nextpage", new StringBuilder().append(page + 1).toString());
		pm.add("prevpage", new StringBuilder().append(page - 1).toString());
		mm.setTitle(pm.process(SoldMenu.title));
		mm.setRow(6);
		mm.build();
		int[] array;
		for (int length = (array = border).length, j = 0; j < length; ++j) {
			final int slot = array[j];
			mm.setItem(slot, SoldMenu.BorderItem);
		}
		mm.setItem(SoldMenu.RefreshItem.slot, pm.process(SoldMenu.RefreshItem.item)).addAction(new Action() {
			@Override
			public void act() {
				SoldMenu.open(p, target, cpage);
			}
		});
		mm.setItem(SoldMenu.WhatIsItem.slot, pm.process(SoldMenu.WhatIsItem.item));
		mm.setItem(SoldMenu.BackItem.slot, pm.process(SoldMenu.BackItem.item)).addAction(new Action() {
			@Override
			public void act() {
				MainMenu.open(p, target, 1);
			}
		});
		if (page + 1 <= maxpage) {
			mm.setItem(SoldMenu.NextPageItem.slot, pm.process(SoldMenu.NextPageItem.clone())).addAction(new Action() {
				@Override
				public void act() {
					SoldMenu.open(p, target, cpage + 1);
				}
			});
		}
		if (page - 1 > 0) {
			mm.setItem(SoldMenu.PrevPageItem.slot, pm.process(SoldMenu.PrevPageItem.clone())).addAction(new Action() {
				@Override
				public void act() {
					SoldMenu.open(p, target, cpage - 1);
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
			final ESoldItem es = sorted.get(i + offset);
			place(mm, p, target, es, itemslot.get(index++), page);
		}
		EPlayer ep = YourAuctionAPI.getEPlayer(target.getUniqueId());
		MyItem toggle = null;
		if (ep.isAutotake()) {
			toggle = OnAutoCollectItem.cloneThis();
		} else {
			toggle = OffAutoCollectItem.cloneThis();
		}
		mm.setItem(toggle.slot, pm.process(toggle.item)).addAction(new Action() {
			@Override
			public void act() {
				ep.setAutotake(!ep.isAutotake());
				ep.save();
				if (ep.isAutotake()) {
					ItemStack toggle = OnAutoCollectItem.clone();
					mm.setItem(OnAutoCollectItem.slot, pm.process(toggle), false);
				} else {
					double total = 0;
					for (ESoldItem es : sorted) {
						if (!es.isTaken()) {
							total += es.getTotalmoney();
							es.setTaken(true);
							es.save();
						}
					}
					if (total > 0) {
						EconomyUT.addBal(p, total);
						MessageUT.plmessage(p, Locales.getValue("success_take_sold"), pm);
						open(p, target, cpage);
					} else {
						ItemStack toggle = OffAutoCollectItem.clone();
						mm.setItem(OffAutoCollectItem.slot, pm.process(toggle), false);
					}
				}
			}
		});
		mm.open(p);
	}

	public static void place(final SoldMenu mm, final Player p, final OfflinePlayer target, final ESoldItem es,
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
		ItemStack av = es.getItem().clone();
		ItemStack icon;
		if (es.isTaken()) {
			icon = SoldMenu.TakenItem.clone();
		} else {
			icon = SoldMenu.SoldItem.clone();
			av = ItemUT.setGlowing(av, true);
		}
		ItemUT.changeDisplayName(av, ItemUT.getName(icon));
		List<String> lore = ItemUT.getLore(av);
		if (lore == null) {
			lore = new ArrayList<>();
		}
		lore.addAll(ItemUT.getLore(icon));
		ItemUT.changeLore(av, lore);
		final ItemStack cacheav = pm.process(av);
		mm.setItem(slot, cacheav).addAction(new Action() {
			@Override
			public void act() {
				if (es.isTaken()) {
					MessageUT.plmessage(p, "<sound>BLOCK_NOTE_PLING");
					return;
				}
				MessageUT.plmessage(p, Locales.getValue("success_take_sold"), pm);
				EconomyUT.addBal(p, es.getTotalmoney());
				es.setTaken(true);
				es.save();
				SoldMenu.open(p, target, page);
			}
		});
	}
}
