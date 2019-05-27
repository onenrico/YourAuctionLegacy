//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.main;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import me.onenrico.yourauction.commands.YourAuction;
import me.onenrico.yourauction.config.EConfig;
import me.onenrico.yourauction.database.Datamanager;
import me.onenrico.yourauction.gui.ExpiredMenu;
import me.onenrico.yourauction.gui.MainMenu;
import me.onenrico.yourauction.gui.SellAllInfo;
import me.onenrico.yourauction.gui.SellallMenu;
import me.onenrico.yourauction.gui.SellerMenu;
import me.onenrico.yourauction.gui.SellingMenu;
import me.onenrico.yourauction.gui.SoldMenu;
import me.onenrico.yourauction.hook.NewPlaceholderAPIHook;
import me.onenrico.yourauction.hook.VaultHook;
import me.onenrico.yourauction.hook.WorldGuardHook;
import me.onenrico.yourauction.listener.CommandListener;
import me.onenrico.yourauction.listener.JoinQuitListener;
import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.MapManager;
import me.onenrico.yourauction.manager.ToggleManager;
import me.onenrico.yourauction.menu.MenuListener;
import me.onenrico.yourauction.menu.MenuLiveUpdate;
import me.onenrico.yourauction.utils.EMaterial;
import me.onenrico.yourauction.utils.EconomyUT;
import me.onenrico.yourauction.utils.ItemUT;
import me.onenrico.yourauction.utils.MathUT;
import me.onenrico.yourauction.utils.MessageUT;
import me.onenrico.yourauction.utils.ReflectionUT;

public class Core extends JavaPlugin {
	private static Core instance;
	public static EConfig configplugin;
	public static EConfig databaseconfig;
	public static EConfig guiconfig;
	public static EConfig animationconfig;
	public static EConfig worthconfig;
	public static ToggleManager buying;
	public static ToggleManager canceling;
	public static boolean thereWorldGuard;

	static {
		Core.buying = new ToggleManager();
		Core.canceling = new ToggleManager();
		Core.thereWorldGuard = false;
	}

	public static Core getThis() {
		return Core.instance;
	}

	@Override
	public void onEnable() {
		Core.instance = this;
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new NewPlaceholderAPIHook().register();
		}
		ReflectionUT.NUMBER_VERSION = MathUT.strInt(ReflectionUT.VERSION.substring(1).replace("_", ""));
	
		Core.configplugin = new EConfig(this, "config.yml");
		Core.databaseconfig = new EConfig(this, "database.yml");
		Core.guiconfig = new EConfig(this, "gui.yml");
		Core.animationconfig = new EConfig(this, "animation.yml");
		Core.worthconfig = new EConfig(this, "worth.yml");
		reloadSetting();
	
		getCommand("yourauction").setExecutor(new YourAuction());
		Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
		Bukkit.getPluginManager().registerEvents(new JoinQuitListener(), this);
		Bukkit.getPluginManager().registerEvents(new MapManager(), this);
		Bukkit.getPluginManager().registerEvents(new CommandListener(), this);
		Datamanager.setup();
		MenuLiveUpdate.startTimer();
		new MetricsLite(this);
		VaultHook.setup();
		EconomyUT.setupEconomy();
		if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
			Core.thereWorldGuard = true;
			WorldGuardHook.getWorldGuard();
			WorldGuardHook.setup();
		}

		for (final Player p : Bukkit.getOnlinePlayers()) {
			final Inventory top = p.getOpenInventory().getTopInventory();
			if (top != null && top.getHolder() != null) {
				final String holder = top.getHolder().toString().toLowerCase();
				if (!holder.contains("me.onenrico") && !holder.contains("<nl>")) {
					continue;
				}
				p.closeInventory();
			}
		}
	}

	public void reloadSetting() {
		Core.configplugin.reload();
		Core.databaseconfig.reload();
		Core.guiconfig.reload();
		Core.animationconfig.reload();
		Core.worthconfig.reload();
		MainMenu.first = true;
		SellingMenu.first = true;
		ExpiredMenu.first = true;
		SoldMenu.first = true;
		SellallMenu.first = true;
		SellAllInfo.first = true;
		SellerMenu.first = true;
		Locales.reload(Core.configplugin.getStr("locales", "none"));
		YourAuction.teks = Locales.getValue("help_message");
		List<String> daliases = new ArrayList<>();
		daliases.add("auc");
		ConfigManager.aliases = Core.configplugin.getStrList("aliases", daliases);
		for (int i = 0; i < ConfigManager.aliases.size(); i++) {
			String al = ConfigManager.aliases.get(i).toLowerCase();
			ConfigManager.aliases.set(i, al);
		}
		ConfigManager.day = Core.configplugin.getStr("time.day", "d");
		ConfigManager.hour = Core.configplugin.getStr("time.hour", "h");
		ConfigManager.minute = Core.configplugin.getStr("time.minute", "m");
		ConfigManager.second = Core.configplugin.getStr("time.second", "s");
		ConfigManager.tax = Core.configplugin.getDouble("auctions.tax", 3.0);
		ConfigManager.salecost = Core.configplugin.getStr("auctions.sale_cost", "50");
		ConfigManager.defaultmaxsell = Core.configplugin.getInt("auctions.default_max_sell", -1);
		ConfigManager.maxprice = Core.configplugin.getDouble("auctions.max_sale_price", -100.0);
		ConfigManager.allowcreative = Core.configplugin.getBool("auctions.allow_creative", false);
		ConfigManager.allowdamaged = Core.configplugin.getBool("auctions.allow_damaged", false);
		ConfigManager.saleHour = Core.configplugin.getDouble("auctions.sale_hour", 30.0);
		ConfigManager.minsaleHour = Core.configplugin.getDouble("auctions.min_sale_hour", 0.8333);
		ConfigManager.destroyHour = Core.configplugin.getDouble("auctions.destroy_hour", 168.0);
		ConfigManager.maxHour = Core.configplugin.getDouble("auctions.max_sale_hour", 30.0);
		ConfigManager.compact = Core.configplugin.getBool("auctions.compact_mode", true);
		ConfigManager.instantautocollect = Core.configplugin.getBool("instant-auto-collect", true);
		ConfigManager.autocollect = Core.configplugin.getBool("auto-collect", false);
		ConfigManager.tools_animation.clear();
		ConfigManager.blocks_animation.clear();
		ConfigManager.animation_enabled = animationconfig.getBool("menu-animation.enabled", true);

		ConfigManager.enabledsellall = Core.configplugin.getBool("sellall.enabled", true);
		ConfigManager.allowcreative2 = Core.configplugin.getBool("sellall.allow_creative", false);
		ConfigManager.allowdamaged2 = Core.configplugin.getBool("sellall.allow_damaged", false);
		ConfigManager.showback = Core.configplugin.getBool("sellall.show_back_button", true);
		ConfigManager.tax2 = Core.configplugin.getDouble("sellall.tax", 0);
		ConfigManager.christmas = Core.configplugin.getBool("christmas", false);

		ConfigManager.blacklist.clear();
		ConfigManager.price_data.clear();
		ConfigManager.ordered_data.clear();
		if (ConfigManager.animation_enabled) {
			for (String m : animationconfig.getStrList("menu-animation.tools")) {
				ConfigManager.tools_animation.add(ItemUT.getItem(m));
			}
			for (String m : animationconfig.getStrList("menu-animation.block")) {
				ConfigManager.blocks_animation.add(ItemUT.getItem(m));
			}
		}
		final List<String> bl = Core.configplugin.getStrList("blacklist", new ArrayList<String>());
		for (final String b : bl) {
			final Material m = EMaterial.fromString(b).parseMaterial();
			if (m != null) {
				ConfigManager.blacklist.add(m);
			} else {
				MessageUT.cmsg("Item " + b + " Not found. Please check your config. OR contact dev");
			}
		}
		ConfigurationSection cs = Core.worthconfig.getConfig().getConfigurationSection("worth");
		if (cs != null) {
			for (String key : cs.getKeys(false)) {
				ConfigurationSection cs2 = cs.getConfigurationSection(key);
				if (cs2 != null) {
					for (String key2 : cs2.getKeys(false)) {
						if (key2.equalsIgnoreCase("*")) {
							for (int i = 0; i < 16; i++) {
								EMaterial em = EMaterial.fromString(key + ":" + i);
								if (em == null) {
									break;
								}
								double price = cs2.getDouble(key2);
								ConfigManager.price_data.put(em, price);
								if(!ConfigManager.ordered_data.contains(em)) {
									ConfigManager.ordered_data.add(em);
								}
							}
						} else {
							EMaterial em = EMaterial.fromString(key + ":" + key2);
							if (em == null) {
								MessageUT.cmsg("Warning! " + key + " Material is not found, please tell the Developer");
								continue;
							}
							double price = cs2.getDouble(key2);
							ConfigManager.price_data.put(em, price);
							if(!ConfigManager.ordered_data.contains(em)) {
								ConfigManager.ordered_data.add(em);
							}
						}
					}
				} else {
					if(key.equals("*")) {
						for(EMaterial em : EMaterial.values()) {
							ConfigManager.price_data.put(em, cs.getDouble(key));
							if(!ConfigManager.ordered_data.contains(em)) {
								ConfigManager.ordered_data.add(em);
							}
						}
					}else {
						EMaterial em = EMaterial.fromString(key);
						if (em == null) {
							MessageUT.cmsg("Warning! " + key + " Material is not found, please tell the Developer");
							continue;
						}
						if (em.isUnique(em) && !em.fromnew) {
							for (int i = 0; i < 16; i++) {
								EMaterial em2 = EMaterial.fromString(key + ":" + i);
								if (em2 == null) {
									break;
								}
								double price = cs.getDouble(key);
								ConfigManager.price_data.put(em2, price);
								if(!ConfigManager.ordered_data.contains(em2)) {
									ConfigManager.ordered_data.add(em2);
								}
							}
						} else {
							em.fromnew = false;
							double price = cs.getDouble(key);
							ConfigManager.price_data.put(em, price);
							if(!ConfigManager.ordered_data.contains(em)) {
								ConfigManager.ordered_data.add(em);
							}
						}
					}
				}
			}
		}
	}
}
