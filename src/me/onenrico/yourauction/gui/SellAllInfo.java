package me.onenrico.yourauction.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.menu.Action;
import me.onenrico.yourauction.menu.GUIMenu;
import me.onenrico.yourauction.menu.MiningAnimation;
import me.onenrico.yourauction.menu.MyItem;
import me.onenrico.yourauction.utils.EMaterial;
import me.onenrico.yourauction.utils.ItemUT;
import me.onenrico.yourauction.utils.MessageUT;

public class SellAllInfo extends GUIMenu {
	private static MyItem BorderItem;
	private static MyItem PrevPageItem;
	private static MyItem NextPageItem;
	private static MyItem SellItem;
	private static MyItem WhatIsItem;
	private static MyItem BackItem;
	private static String title;
	public static Boolean first;
	private static HashMap<UUID, SellAllInfo> cache;

	static {
		SellAllInfo.first = true;
		SellAllInfo.cache = new HashMap<>();
	}

	public static SellAllInfo setup(final UUID uuid) {
		final SellAllInfo mm = SellAllInfo.cache.getOrDefault(uuid, new SellAllInfo());
		if (mm.getName() == null) {
			mm.setName("SellAllInfoMenu");
			SellAllInfo.cache.put(uuid, mm);
		}
		if (SellAllInfo.first) {
			SellAllInfo.title = Core.guiconfig.getStr(String.valueOf(mm.getName()) + ".Title", "&cNot Defined");
			SellAllInfo.BorderItem = GUIMenu.setupItem(mm, "BorderItem", -1);
			SellAllInfo.PrevPageItem = GUIMenu.setupItem(mm, "PrevPageItem", 48);
			SellAllInfo.NextPageItem = GUIMenu.setupItem(mm, "NextPageItem", 50);
			SellAllInfo.SellItem = GUIMenu.setupItem(mm, "SellItem", -1);
			SellAllInfo.WhatIsItem = GUIMenu.setupItem(mm, "WhatIsItem", 53);
			SellAllInfo.BackItem = GUIMenu.setupItem(mm, "BackItem", 4);
			SellAllInfo.first = false;
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
		MessageUT.plmessage(p, Locales.getValue("sell_all_info_menu"));
		final SellAllInfo mm = setup(p.getUniqueId());
		final PlaceholderManager pm = new PlaceholderManager(Locales.pm);
		final List<EMaterial> sorted = ConfigManager.ordered_data;
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
		pm.add("page", new StringBuilder().append(page).toString());
		pm.add("maxpage", new StringBuilder().append(maxpage).toString());
		pm.add("nextpage", new StringBuilder().append(page + 1).toString());
		pm.add("prevpage", new StringBuilder().append(page - 1).toString());
		mm.setTitle(pm.process(SellAllInfo.title));
		mm.setRow(6);
		mm.build();
		int[] array;
		for (int length = (array = border).length, j = 0; j < length; ++j) {
			final int slot = array[j];
			mm.setItem(slot, SellAllInfo.BorderItem);
		}
		mm.setItem(SellAllInfo.WhatIsItem.slot, pm.process(SellAllInfo.WhatIsItem.item));
		mm.setItem(SellAllInfo.BackItem.slot, pm.process(SellAllInfo.BackItem.item)).addAction(new Action() {
			@Override
			public void act() {
				SellallMenu.open(p, target);
			}
		});
		if (page + 1 <= maxpage) {
			mm.setItem(SellAllInfo.NextPageItem.slot, pm.process(SellAllInfo.NextPageItem.clone())).addAction(new Action() {
				@Override
				public void act() {
					SellAllInfo.open(p, target, cpage + 1);
				}
			});
		}
		if (page - 1 > 0) {
			mm.setItem(SellAllInfo.PrevPageItem.slot, pm.process(SellAllInfo.PrevPageItem.clone())).addAction(new Action() {
				@Override
				public void act() {
					SellAllInfo.open(p, target, cpage - 1);
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
			final EMaterial em = sorted.get(i + offset);
			place(mm, p, target, em, itemslot.get(index++), page);
		}
		mm.open(p);
	}

	public static void place(final SellAllInfo mm, final Player p, final OfflinePlayer target, final EMaterial es,
			final int slot, final int page) {
		final PlaceholderManager pm = new PlaceholderManager();
		pm.add("itemname", "");
		pm.add("price", ConfigManager.price_data.getOrDefault(es, 0d));
		pm.add("nextpage", new StringBuilder().append(page + 1).toString());
		pm.add("prevpage", new StringBuilder().append(page - 1).toString());
		final ItemStack icon = SellAllInfo.SellItem.clone();
		final ItemStack av = es.parseItem().clone();
		ItemUT.changeDisplayName(av, ItemUT.getName(icon));
		List<String> lore = ItemUT.getLore(av);
		if (lore == null) {
			lore = new ArrayList<>();
		}
		lore.addAll(ItemUT.getLore(icon));
		ItemUT.changeLore(av, lore);
		final ItemStack cacheav = av;
		mm.setItem(slot, cacheav, pm);
	}
}
