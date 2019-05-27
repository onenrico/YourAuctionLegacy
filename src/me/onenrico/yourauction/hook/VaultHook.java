//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.utils.MessageUT;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class VaultHook {
	private static Core instance;
	public static Economy v_economy = null;
	public static Permission v_permission = null;
	public static Chat v_chat = null;


	public static void setup() {
		VaultHook.instance = Core.getThis();
		if (setupEconomy()) {
			setupPermissions();
		}
	}

	public static boolean setupEconomy() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
			MessageUT.cmsg("Vault not found...");
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (rsp == null) {
			MessageUT.cmsg("RSP Vault not found...");
			return false;
		}
		VaultHook.v_economy = rsp.getProvider();
		return VaultHook.v_economy != null;
	}

	public static boolean setupPermissions() {
		try {
			final RegisteredServiceProvider<Permission> rsp = VaultHook.instance.getServer().getServicesManager()
					.getRegistration((Class) Permission.class);
			VaultHook.v_permission = rsp.getProvider();
		} catch (Exception ex) {
			return false;
		}
		return VaultHook.v_permission != null;
	}
}
