package me.onenrico.yourauction.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.onenrico.yourauction.gui.ExpiredMenu;
import me.onenrico.yourauction.gui.MainMenu;
import me.onenrico.yourauction.gui.SellallMenu;
import me.onenrico.yourauction.gui.SellingMenu;
import me.onenrico.yourauction.gui.SoldMenu;
import me.onenrico.yourauction.hook.WorldGuardHook;
import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.main.YourAuctionAPI;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.menu.GUIMenu;
import me.onenrico.yourauction.menu.SmileAnimation;
import me.onenrico.yourauction.object.ESaleItem;
import me.onenrico.yourauction.utils.EMaterial;
import me.onenrico.yourauction.utils.EconomyUT;
import me.onenrico.yourauction.utils.ItemUT;
import me.onenrico.yourauction.utils.MathUT;
import me.onenrico.yourauction.utils.MessageUT;
import me.onenrico.yourauction.utils.PermissionUT;
import me.onenrico.yourauction.utils.PlayerUT;
import me.onenrico.yourauction.utils.TimeManager;

public class YourAuction implements CommandExecutor {
	String prefix;
	public static List<String> teks = new ArrayList<>();

	public YourAuction() {
		prefix = "";
	}

	public void setup(final Player p) {
		if (teks.isEmpty()) {
			teks = Locales.getValue("help_message");
		}
	}

	public void help(final Player p, final int page) {
		setup(p);
		Help.setup(YourAuction.teks, 7);
		Help.send(p, page);
	}

	public void help(final Player p, final String msg) {
		setup(p);
		Help.setup(YourAuction.teks, 6);
		Help.send(p, msg);
	}

	public Integer getId(final String arg) {
		Integer result = null;
		try {
			result = Integer.parseInt(arg);
		} catch (NumberFormatException ex) {
			return null;
		}
		return result;
	}

	public Long getLong(final String arg) {
		Long result = null;
		try {
			result = Long.parseLong(arg);
		} catch (NumberFormatException ex) {
			return null;
		}
		return result;
	}

	public Double getPrice(final String arg) {
		Double result = null;
		try {
			result = Double.parseDouble(arg);
		} catch (NumberFormatException ex) {
			return null;
		}
		return result;
	}

	@SuppressWarnings("deprecation")
	public void handle(final Player p, final String[] args) {
		if (Core.thereWorldGuard && !WorldGuardHook.canOpen(p)) {
			MessageUT.plmessage(p, Locales.getValue("exceed_worldguard"));
			return;
		}
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("debug") && p.getName().equals("GantzID")) {
				GUIMenu gm = new GUIMenu("test", "Enrico Tampan", 6);
				gm.setAnimation(new SmileAnimation());
				gm.setItem(3, EMaterial.DIAMOND.parseItem());
				gm.setItem(6, EMaterial.DIAMOND.parseItem());
				gm.setItem(50, EMaterial.DIAMOND.parseItem());
				gm.setItem(24, EMaterial.DIAMOND.parseItem());
				gm.setItem(31, EMaterial.DIAMOND.parseItem());
				gm.open(()->MessageUT.cmsg("opened"), p);
				return;
			}
			if (args[0].equalsIgnoreCase("help")) {
				if (args.length > 1) {
					final Integer page = getId(args[1]);
					if (page == null) {
						this.help(p, args[1]);
					} else {
						this.help(p, (int) MathUT.clamp(page, 1L));
					}
					return;
				}
				this.help(p, 1);
				return;
			} else if (args[0].equalsIgnoreCase("sell")) {
				if (!ConfigManager.allowcreative && p.getGameMode().equals(GameMode.CREATIVE)) {
					MessageUT.plmessage(p, Locales.getValue("exceed_creative"));
					return;
				}
				if (!PermissionUT.check(p, "ya.sell")) {
					return;
				}
				if (args.length != 2 && args.length != 3) {
					this.help(p, "sell");
					return;
				}
				Long time = -1l;
				if (args.length == 3) {
					time = getLong(args[2]);
					PlaceholderManager pm = new PlaceholderManager();
					pm.add("maximum", (int) (ConfigManager.maxHour * 3600));
					pm.add("minimum", (int) (ConfigManager.minsaleHour * 3600));
					if (time == null || time < 0) {
						MessageUT.plmessage(p, Locales.getValue("must_number"));
						return;
					}
					if (time == 0) {
						if (!p.hasPermission("ya.noexpire")) {
							MessageUT.plmessage(p, Locales.getValue("exceed_time"), pm);
							return;
						}
					}
					double dt = time;
					if ((dt / 3600d) > ConfigManager.maxHour || (dt / 3600d) < ConfigManager.minsaleHour) {
						MessageUT.plmessage(p, Locales.getValue("exceed_time"), pm);
						return;
					}
				}
				final Double price = getPrice(args[1]);
				if (price == null || price < 0.0) {
					MessageUT.plmessage(p, Locales.getValue("must_number"));
					return;
				}
				final ItemStack item = PlayerUT.getHand(p);
				if (item == null || item.getType().equals(Material.AIR)) {
					MessageUT.plmessage(p, Locales.getValue("must_hold"));
					return;
				}
				if (!ConfigManager.allowdamaged && EMaterial.isDamageable(EMaterial.fromMaterial(item.getType()))
						&& item.getDurability() > 0) {
					MessageUT.plmessage(p, Locales.getValue("exceed_damaged"));
					return;
				}
				double salecost = 0;
				if(ConfigManager.salecost.contains("%")) {
					salecost = (price * Double.parseDouble(StringUtils.strip(ConfigManager.salecost.replace("%", ""))) / 100d );
				}else {
					salecost = Double.parseDouble(ConfigManager.salecost);
				}
				if (salecost > 0) {
					if (!EconomyUT.has(p, salecost)) {
						final PlaceholderManager pm = new PlaceholderManager();
						pm.add("money", salecost);
						MessageUT.plmessage(p, Locales.getValue("insufficient_money"), pm);
						return;
					}
				}
				if (ESaleItem.table.getOwned(p.getUniqueId()).size() >= YourAuctionAPI.getMaxSale(p)) {
					MessageUT.plmessage(p, Locales.getValue("exceed_sale"));
					return;
				}
				if (price > ConfigManager.maxprice) {
					MessageUT.plmessage(p, Locales.getValue("exceed_price"));
					return;
				}
				if (ConfigManager.blacklist.contains(item.getType())) {
					MessageUT.plmessage(p, Locales.getValue("exceed_blacklist"));
					return;
				}
				EconomyUT.subtractBal(p, salecost);
				final ESaleItem es = ESaleItem.create(p.getUniqueId(), item, price);
				final PlaceholderManager pm = es.getPlaceholder();
				if (time > 0) {
					es.setExpirydate(System.currentTimeMillis() + (time * 1000));
					pm.add("expire", TimeManager.formatTime(TimeManager.getSecond(System.currentTimeMillis(),
							System.currentTimeMillis() + (time * 1000))));
				} else if (time == 0) {
					es.setExpirydate(0);
					pm.add("expire", "N/A");
				} else {
					if (ConfigManager.saleHour < 0) {
						pm.add("expire", "N/A");
					} else {
						pm.add("expire", TimeManager.formatTime(TimeManager.getSecond(System.currentTimeMillis(),
								System.currentTimeMillis() + (long) (ConfigManager.saleHour * 3600000.0))));
					}
				}
				es.save();
				PlayerUT.setHand(p, ItemUT.createItem(Material.AIR));
				if ((pm.get("itemname").equals(""))) {
					pm.add("itemname", es.getItem().getType().toString());
				}
				pm.add("seller", p.getName());
				for(Player other : Bukkit.getOnlinePlayers()) {
					MessageUT.plmessage(other, Locales.getValue("sale_item"), pm);
				}
				MessageUT.plmessage(p, Locales.getValue("success_sale"), pm);
				return;
			} else {
				if (args[0].equalsIgnoreCase("sold")) {
					if (PermissionUT.check(p, "ya.access")) {
						SoldMenu.open(p, p, 1);
					}
					return;
				}
				if (args[0].equalsIgnoreCase("selling")) {
					if (PermissionUT.check(p, "ya.access")) {
						SellingMenu.open(p, p, 1);
					}
					return;
				}
				if (args[0].equalsIgnoreCase("sellall")) {
					if (PermissionUT.check(p, "ya.access.sellall")) {
						SellallMenu.open(p, p);
					}
					return;
				}
				if (args[0].equalsIgnoreCase("expired")) {
					if (PermissionUT.check(p, "ya.access")) {
						ExpiredMenu.open(p, p, 1);
					}
					return;
				}
				if (args[0].equalsIgnoreCase("reload")) {
					if (!PermissionUT.check(p, "ya.reload")) {
						return;
					}
					if (args.length != 1) {
						this.help(p, "reload");
						return;
					}
					if (!PermissionUT.check(p, "ya.reload")) {
						return;
					}
					Core.getThis().reloadSetting();
					MessageUT.plmessage(p, Locales.getValue("config_reload"));
					return;
				}
			}
		}
		if (PermissionUT.check(p, "ya.access")) {
			MainMenu.open(p, p, 1);
		}
	}

	@Override
	public boolean onCommand(final CommandSender cs, final Command cmd, final String label, final String[] args) {
		Player p = null;
		if (cs instanceof Player) {
			p = (Player) cs;
			handle(p, args);
			return true;
		}
		if (args.length < 1) {
			YourAuction.teks.clear();
			YourAuction.teks.add("&b/yourauction reload&7- &freload the plugin");
			for (final String msg : YourAuction.teks) {
				MessageUT.cmsg(msg);
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("reload")) {
			Core.getThis().reloadSetting();
			MessageUT.cmsg("Config Reloaded..!");
			return true;
		}
		return true;
	}
}
