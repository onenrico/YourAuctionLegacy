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
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
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
import me.onenrico.yourauction.menu.MenuItem;
import me.onenrico.yourauction.menu.MiningAnimation;
import me.onenrico.yourauction.menu.MyItem;
import me.onenrico.yourauction.object.EBidItem;
import me.onenrico.yourauction.object.EExpiredItem;
import me.onenrico.yourauction.object.ESaleItem;
import me.onenrico.yourauction.object.EShowItem;
import me.onenrico.yourauction.object.ESoldItem;
import me.onenrico.yourauction.utils.EMaterial;
import me.onenrico.yourauction.utils.EconomyUT;
import me.onenrico.yourauction.utils.ItemUT;
import me.onenrico.yourauction.utils.MathUT;
import me.onenrico.yourauction.utils.MessageUT;
import me.onenrico.yourauction.utils.PermissionUT;
import me.onenrico.yourauction.utils.ReflectionUT;

public class MainMenu extends GUIMenu {
	private static MyItem BorderItem;
	private static MyItem PrevPageItem;
	private static MyItem NextPageItem;
	private static MyItem RefreshItem;
	private static MyItem SellItem;
	private static MyItem BidItem;
	private static MyItem CurrentlySellItem;
	private static MyItem ExpiredItem;
	private static MyItem SellallItem;
	private static MyItem SoldItem;
	private static MyItem SellerItem;
	private static MyItem WhatIsItem;
	private static MyItem HowToItem;
	private static MyItem SelfBuyItem;
	private static MyItem ConfirmBuyItem;
	private static MyItem InventoryFullItem;
	private static MyItem NotEnoughMoneyItem;
	private static String title;
	public static Boolean first;
	private static HashMap<UUID, MainMenu> cache;

	static {
		MainMenu.first = true;
		MainMenu.cache = new HashMap<>();
	}

	public static MainMenu setup(final UUID uuid) {
		final MainMenu mm = MainMenu.cache.getOrDefault(uuid, new MainMenu());
		if (mm.getName() == null) {
			mm.setName("AuctionMenu");
			MainMenu.cache.put(uuid, mm);
		}
		if (MainMenu.first) {
			MainMenu.BorderItem = GUIMenu.setupItem(mm, "BorderItem", -1);
			MainMenu.PrevPageItem = GUIMenu.setupItem(mm, "PrevPageItem", 48);
			MainMenu.NextPageItem = GUIMenu.setupItem(mm, "NextPageItem", 50);
			MainMenu.RefreshItem = GUIMenu.setupItem(mm, "RefreshItem", 49);
			MainMenu.SellItem = GUIMenu.setupItem(mm, "SellItem", -1);
			MainMenu.CurrentlySellItem = GUIMenu.setupItem(mm, "CurrentlySellItem", 36);
			MainMenu.ExpiredItem = GUIMenu.setupItem(mm, "ExpiredItem", 27);
			MainMenu.SellallItem = GUIMenu.setupItem(mm, "SellallItem", 9);
			MainMenu.SoldItem = GUIMenu.setupItem(mm, "SoldItem", 18);
			MainMenu.WhatIsItem = GUIMenu.setupItem(mm, "WhatIsItem", 53);
			MainMenu.SellerItem = GUIMenu.setupItem(mm, "SellerItem", 52);
			MainMenu.HowToItem = GUIMenu.setupItem(mm, "HowToItem", 4);
			MainMenu.SelfBuyItem = GUIMenu.setupItem(mm, "SelfBuyItem", -1);
			MainMenu.ConfirmBuyItem = GUIMenu.setupItem(mm, "ConfirmBuyItem", -1);
			MainMenu.InventoryFullItem = GUIMenu.setupItem(mm, "InventoryFullItem", -1);
			MainMenu.NotEnoughMoneyItem = GUIMenu.setupItem(mm, "NotEnoughMoney", -1);
			MainMenu.title = Core.guiconfig.getStr(String.valueOf(mm.getName()) + ".Title", "&cNot Defined");
			MainMenu.first = false;
		}
		if (ConfigManager.animation_enabled) {
			mm.setAnimation(new MiningAnimation());
		}
		mm.getMenuitems().clear();
		return mm;
	}

	@SuppressWarnings("deprecation")
	public static void open(final Player p, final OfflinePlayer target, int page) {
		MessageUT.plmessage(p, Locales.getValue("main_menu"));
		final MainMenu mm = setup(p.getUniqueId());
		final PlaceholderManager pm = new PlaceholderManager(Locales.pm);
		List<EShowItem> sorted = null;
		boolean other = false;
		if (target.getUniqueId().equals(p.getUniqueId())) {
			sorted = YourAuctionAPI.getShowItems();
		} else {
			sorted = YourAuctionAPI.getShowItems(target.getUniqueId());
			other = true;
		}
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
		if (!other) {
			pm.add("selling", new StrLookup() {
				@Override
				public String lookup(String msg) {
					return new StringBuilder().append(ESaleItem.table.getOwned(p.getUniqueId()).size()).toString();
				}
			});
			pm.add("expired", new StrLookup() {
				@Override
				public String lookup(String msg) {
					return new StringBuilder().append(EExpiredItem.table.getOwned(p.getUniqueId()).size()).toString();
				}
			});
			pm.add("sold", new StrLookup() {
				@Override
				public String lookup(String msg) {
					return new StringBuilder().append(ESoldItem.table.getOwned(p.getUniqueId()).size()).toString();
				}
			});
			pm.add("revenue", new StrLookup() {
				@Override
				public String lookup(String msg) {
					double revenue = 0.0;
					for (final EObject eo : ESoldItem.table.getOwned(target.getUniqueId())) {
						final ESoldItem es = (ESoldItem) eo;
						if (!es.isTaken()) {
							revenue += es.getTotalmoney();
						}
					}
					return new StringBuilder().append(MathUT.format(revenue)).toString();
				}
			});
			pm.add("total", new StrLookup() {
				@Override
				public String lookup(String msg) {
					double total = 0.0;
					for (final EObject eo : ESoldItem.table.getOwned(target.getUniqueId())) {
						final ESoldItem es = (ESoldItem) eo;
						total += es.getTotalmoney();
					}
					return new StringBuilder().append(MathUT.format(total)).toString();
				}
			});
		}
		pm.add("page", new StringBuilder().append(page).toString());
		pm.add("maxpage", new StringBuilder().append(maxpage).toString());
		pm.add("nextpage", new StringBuilder().append(page + 1).toString());
		pm.add("prevpage", new StringBuilder().append(page - 1).toString());
		mm.setTitle(pm.process(MainMenu.title));
		mm.setRow(6);
		mm.build(true);
		int[] array;
		for (int length = (array = border).length, j = 0; j < length; ++j) {
			final int slot = array[j];
			mm.setItem(slot, MainMenu.BorderItem);
		}
		final ItemStack av = SellerItem.clone();
		EMaterial avm = EMaterial.fromMaterial(av.getType());
		if (avm.name().equals(EMaterial.PLAYER_HEAD.name())) {
			SkullMeta sm = (SkullMeta) av.getItemMeta();
			sm.setOwner(p.getName());
			if(!ReflectionUT.VERSION.contains("8")) {
				sm.setOwningPlayer(p);
			}
			av.setItemMeta(sm);
		}
		mm.setItem(SellerItem.slot, av, pm).addAction(new Action() {
			@Override
			public void act() {
				SellerMenu.open(p, target, 1);
			}
		});
		if (!other) {
			MyItem sellClone = MainMenu.CurrentlySellItem.cloneThis();
			if (YourAuctionAPI.getSaleItems(target.getUniqueId()).size() > 0) {
				sellClone.item = ItemUT.setGlowing(sellClone.item, true);
			}
			mm.setItem(sellClone, pm).addAction(new Action() {
				@Override
				public void act() {
					SellingMenu.open(p, target, 1);
				}
			});
			MyItem expiredClone = MainMenu.ExpiredItem.cloneThis();
			if (YourAuctionAPI.getExpiredItems(target.getUniqueId()).size() > 0) {
				expiredClone.item = ItemUT.setGlowing(expiredClone.item, true);
			}
			mm.setItem(expiredClone, pm).addAction(new Action() {
				@Override
				public void act() {
					ExpiredMenu.open(p, target, 1);
				}
			});
			MyItem soldClone = MainMenu.SoldItem.cloneThis();
			for (ESoldItem esi : YourAuctionAPI.getSoldItems(target.getUniqueId())) {
				if (!esi.isTaken()) {
					soldClone.item = ItemUT.setGlowing(soldClone.item, true);
					break;
				}
			}
			mm.setItem(soldClone, pm).addAction(new Action() {
				@Override
				public void act() {
					SoldMenu.open(p, target, 1);
				}
			});
			if (ConfigManager.enabledsellall) {
				mm.setItem(MainMenu.SellallItem.slot, SellallItem, pm).addAction(new Action() {
					@Override
					public void act() {
						if (PermissionUT.check(p, "ya.access.sellall")) {
							if (!ConfigManager.allowcreative2) {
								if (p.getGameMode().equals(GameMode.CREATIVE)) {
									MessageUT.plmessage(p, Locales.getValue("exceed_creative"));
									return;
								}
							}
							SellallMenu.open(p, target);
						}
					}
				});
			}
		}
		mm.setItem(MainMenu.RefreshItem.slot, pm.process(MainMenu.RefreshItem.item)).addAction(new Action() {
			@Override
			public void act() {
				MainMenu.open(p, target, cpage);
			}
		});
		mm.setItem(MainMenu.WhatIsItem.slot, pm.process(MainMenu.WhatIsItem.item));
		mm.setItem(MainMenu.HowToItem.slot, pm.process(MainMenu.HowToItem.item));
		if (page + 1 <= maxpage) {
			mm.setItem(MainMenu.NextPageItem.slot, pm.process(MainMenu.NextPageItem.clone())).addAction(new Action() {
				@Override
				public void act() {
					MainMenu.open(p, target, cpage + 1);
				}
			});
		}
		if (page - 1 > 0) {
			mm.setItem(MainMenu.PrevPageItem.slot, pm.process(MainMenu.PrevPageItem.clone())).addAction(new Action() {
				@Override
				public void act() {
					MainMenu.open(p, target, cpage - 1);
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
			final EShowItem es = sorted.get(i + offset);
			place(mm, p, target, es, itemslot.get(index++), page);
		}
		mm.open(p);
	}

	@SuppressWarnings("unused")
	public static void place(final MainMenu mm, final Player p, final OfflinePlayer target, final EShowItem es,
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
		boolean saleitem = es instanceof ESaleItem;
		ItemStack icon = null;
		if (saleitem) {
			icon = MainMenu.SellItem.clone();
		} else {
			icon = MainMenu.BidItem.clone();
		}
		ItemStack av = es.getItem().clone();
		ItemUT.changeDisplayName(av, ItemUT.getName(icon));
		List<String> lore = ItemUT.getLore(av);
		if (lore == null) {
			lore = new ArrayList<>();
		}
		lore.addAll(ItemUT.getLore(icon));
		av = ItemUT.changeLore(av, lore);
		final ItemStack bv = MainMenu.ConfirmBuyItem.clone();
		final ItemStack cacheav = av;
		MenuItem mi = mm.setItem(slot, cacheav, pm).addAction(new Action(ClickType.SHIFT_RIGHT) {
			@Override
			public void act() {
				if (saleitem) {
					if (!ESaleItem.table.getLoadedData().contains((ESaleItem)es)) {
						MainMenu.open(p, target, page);
						return;
					}
				} else {
					if (!EBidItem.table.getLoadedData().contains((EBidItem)es)) {
						MainMenu.open(p, target, page);
						return;
					}
				}
				if (p.hasPermission("ya.admin")) {
					EExpiredItem.create(es);
					open(p, target, page);
					return;
				}
			}
		}).addAction(new Action(ClickType.LEFT) {
			@Override
			public void act() {
				if (saleitem) {
					if (!ESaleItem.table.getLoadedData().contains((ESaleItem)es)) {
						MainMenu.open(p, target, page);
						return;
					}
				} else {
					if (!EBidItem.table.getLoadedData().contains((EBidItem)es)) {
						MainMenu.open(p, target, page);
						return;
					}
				}
				if (es.isExpired()) {
					EExpiredItem.create(es);
					MainMenu.open(p, target, page);
					return;
				}
				final MenuItem cache = mm.getMenuItem(slot).clone();
				if (es.getOwner().equals(p.getUniqueId())) {
//				if(false) {
					mm.setItem(slot, pm.process(MainMenu.SelfBuyItem.item)).addAction(new Action() {
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
					return;
				}
				if (!Core.buying.isAllow(p.getUniqueId())) {
					double price = 0;
					if (saleitem) {
						price = ((ESaleItem) es).getPrice();
					} else {

					}
					if (EconomyUT.has(p, price)) {
						final double fprice = price;
						if (p.getInventory().firstEmpty() != -1) {
							Core.buying.add(p.getUniqueId());
							mm.setItem(slot, pm.process(bv)).addAction(new Action() {
								@Override
								public void act() {
									if (saleitem) {
										if (!ESaleItem.table.getLoadedData().contains((ESaleItem)es)) {
											MainMenu.open(p, target, page);
											return;
										}
									} else {
										if (!EBidItem.table.getLoadedData().contains((EBidItem)es)) {
											MainMenu.open(p, target, page);
											return;
										}
									}
									if (es.isExpired()) {
										EExpiredItem.create(es);
										MainMenu.open(p, target, page);
										return;
									}
									EconomyUT.subtractBal(p, fprice);
									Core.buying.remove(p.getUniqueId());
									cache.setSlot(-1);
									p.getInventory().addItem(new ItemStack[] { es.getItem().clone() });
									ESoldItem.create(es, p.getUniqueId());
									MainMenu.open(p, target, page);
									if ((pm.get("itemname").equals(""))) {
										pm.add("itemname", es.getItem().getType().toString());
									}
									MessageUT.plmessage(p, Locales.getValue("success_buy"), pm);
								}
							});
							new BukkitRunnable() {
								@Override
								public void run() {
									if (cache.getSlot() < 0) {
										return;
									}
									Core.buying.remove(p.getUniqueId());
									mm.setItem(slot, cache);
								}
							}.runTaskLater(Core.getThis(), 40L);
						} else {
							mm.setItem(slot, pm.process(MainMenu.InventoryFullItem.item)).addAction(new Action() {
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
						mm.setItem(slot, pm.process(MainMenu.NotEnoughMoneyItem.item)).addAction(new Action() {
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
