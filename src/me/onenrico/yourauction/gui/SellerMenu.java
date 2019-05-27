//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.onenrico.yourauction.database.EObject;
import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.main.YourAuctionAPI;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.menu.Action;
import me.onenrico.yourauction.menu.GUIMenu;
import me.onenrico.yourauction.menu.MiningAnimation;
import me.onenrico.yourauction.menu.MyItem;
import me.onenrico.yourauction.object.ESaleItem;
import me.onenrico.yourauction.object.ESoldItem;
import me.onenrico.yourauction.utils.EMaterial;
import me.onenrico.yourauction.utils.MathUT;
import me.onenrico.yourauction.utils.MessageUT;
import me.onenrico.yourauction.utils.ReflectionUT;

public class SellerMenu extends GUIMenu {
	private static MyItem BorderItem;
	private static MyItem PrevPageItem;
	private static MyItem NextPageItem;
	private static MyItem SellerItem;
	private static MyItem WhatIsItem;
	private static MyItem BackItem;
	private static String title;
	public static Boolean first;
	private static HashMap<UUID, SellerMenu> cache;

	static {
		SellerMenu.first = true;
		SellerMenu.cache = new HashMap<>();
	}

	public static SellerMenu setup(final UUID uuid) {
		final SellerMenu mm = SellerMenu.cache.getOrDefault(uuid, new SellerMenu());
		if (mm.getName() == null) {
			mm.setName("SellerMenu");
			SellerMenu.cache.put(uuid, mm);
		}
		if (SellerMenu.first) {
			SellerMenu.title = Core.guiconfig.getStr(String.valueOf(mm.getName()) + ".Title", "&cNot Defined");
			SellerMenu.BorderItem = GUIMenu.setupItem(mm, "BorderItem", -1);
			SellerMenu.PrevPageItem = GUIMenu.setupItem(mm, "PrevPageItem", 48);
			SellerMenu.NextPageItem = GUIMenu.setupItem(mm, "NextPageItem", 50);
			SellerMenu.SellerItem = GUIMenu.setupItem(mm, "SellerItem", -1);
			SellerMenu.WhatIsItem = GUIMenu.setupItem(mm, "WhatIsItem", 53);
			SellerMenu.BackItem = GUIMenu.setupItem(mm, "BackItem", 4);
			SellerMenu.first = false;
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
		MessageUT.plmessage(p, Locales.getValue("seller_menu"));
		final SellerMenu mm = setup(p.getUniqueId());
		final PlaceholderManager pm = new PlaceholderManager(Locales.pm);
		final List<UUID> owner = new ArrayList<>(YourAuctionAPI.getSaleItemsOwners());
		final int alldata = owner.size();
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
		mm.setTitle(pm.process(SellerMenu.title));
		mm.setRow(6);
		mm.build();
		int[] array;
		for (int length = (array = border).length, j = 0; j < length; ++j) {
			final int slot = array[j];
			mm.setItem(slot, SellerMenu.BorderItem);
		}
		mm.setItem(SellerMenu.WhatIsItem.slot, pm.process(SellerMenu.WhatIsItem.item));
		mm.setItem(SellerMenu.BackItem.slot, pm.process(SellerMenu.BackItem.item)).addAction(new Action() {
			@Override
			public void act() {
				MainMenu.open(p, target, 1);
			}
		});
		if (page + 1 <= maxpage) {
			mm.setItem(SellerMenu.NextPageItem.slot, pm.process(SellerMenu.NextPageItem.clone())).addAction(new Action() {
				@Override
				public void act() {
					SellerMenu.open(p, target, cpage + 1);
				}
			});
		}
		if (page - 1 > 0) {
			mm.setItem(SellerMenu.PrevPageItem.slot, pm.process(SellerMenu.PrevPageItem.clone())).addAction(new Action() {
				@Override
				public void act() {
					SellerMenu.open(p, target, cpage - 1);
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
			final UUID sellerUUID = owner.get(i + offset);
			place(mm, p, target, sellerUUID, itemslot.get(index++), page);
		}
		mm.open(p);
	}

	private static HashMap<UUID, ItemStack> skullcache = new HashMap<>();

	@SuppressWarnings("deprecation")
	public static void place(final SellerMenu mm, final Player p, final OfflinePlayer target, final UUID sellerUUID,
			final int slot, final int page) {
		final PlaceholderManager pm = new PlaceholderManager();
		OfflinePlayer seller = Bukkit.getOfflinePlayer(sellerUUID);
		Set<EObject> solds = ESoldItem.table.getOwned(sellerUUID);
		Set<EObject> sales = ESaleItem.table.getOwned(sellerUUID);
		double total = 0.0;
		for (final EObject eo : solds) {
			final ESoldItem es = (ESoldItem) eo;
			total += es.getTotalmoney();
		}
		pm.add("selling", sales.size());
		pm.add("sold", solds.size());
		pm.add("total", MathUT.format(total));
		pm.add("name", seller.getName());

		ItemStack av = null;
		if (skullcache.containsKey(sellerUUID)) {
			av = skullcache.get(sellerUUID);
		} else {
			av = SellerItem.clone();
			EMaterial avm = EMaterial.fromString(av.getType().toString()+":"+av.getDurability());
			if(av.getType().toString().equalsIgnoreCase("PLAYER_HEAD")) {
				avm = EMaterial.PLAYER_HEAD;
			}
			if (avm.name().equals(EMaterial.PLAYER_HEAD.name())) {
				final ItemStack av2 = av;
				new BukkitRunnable() {
					@Override
					public void run() {
						SkullMeta sm = (SkullMeta) av2.getItemMeta();
						if(!ReflectionUT.VERSION.contains("8")) {
							sm.setOwningPlayer(seller);
						}
						av2.setItemMeta(sm);
						skullcache.put(seller.getUniqueId(), av2);
					}
				}.runTask(Core.getThis());
			}else {
				skullcache.put(seller.getUniqueId(), av);
			}
		}
		mm.setItem(slot, av.clone(), pm).addAction(new Action() {
			@Override
			public void act() {
				MainMenu.open(p, seller, 1);
			}
		});
	}
}
