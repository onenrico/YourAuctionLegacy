//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.onenrico.yourauction.hook.VaultHook;
import me.onenrico.yourauction.main.Core;
import net.milkbowl.vault.economy.EconomyResponse;

public class EconomyUT {
	Core instance;
	private static PlayerPoints pp;
	private static int mode = 0;

	public EconomyUT() {
		instance = Core.getThis();
	}

	@SuppressWarnings("unused")
	private static boolean isPP() {
		final Plugin plugin = Core.getThis().getServer().getPluginManager().getPlugin("PlayerPoints");
		return plugin != null;
	}

	public static void setupEconomy() {
	}

	public static double round(final double value, final int places) {
		if (places < 0) {
			throw new IllegalArgumentException();
		}
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.DOWN);
		return bd.doubleValue();
	}

	public static double getRawBal(final Player player) {
		final double bal = VaultHook.v_economy.getBalance(player);
		return round(bal, 2);
	}

	public static Boolean has(final Player player, final double amount) {
		if (EconomyUT.mode != 1) {
			return VaultHook.v_economy.has(player, amount);
		}
		final int balance = EconomyUT.pp.getAPI().look(player.getUniqueId());
		if (balance >= amount) {
			return true;
		}
		return false;
	}

	public static String format(final double amount) {
		return VaultHook.v_economy.format(amount);
	}

	public static String getBal(final Player player) {
		final String bal = format(getRawBal(player));
		return bal;
	}

	public static EconomyResponse addBal(final Player player, final double amount) {
		if (EconomyUT.mode == 1) {
			EconomyUT.pp.getAPI().give(player.getUniqueId(), (int) amount);
			return null;
		}
		final EconomyResponse trans = VaultHook.v_economy.depositPlayer(player, amount);
		return trans;
	}

	public static EconomyResponse addBal(final UUID player, final double amount) {
		if (EconomyUT.mode == 1) {
			EconomyUT.pp.getAPI().give(player, (int) amount);
			return null;
		}
		final EconomyResponse trans = VaultHook.v_economy.depositPlayer(Bukkit.getOfflinePlayer(player), amount);
		return trans;
	}

	public static EconomyResponse subtractBal(final Player player, final double amount) {
		if (EconomyUT.mode == 1) {
			EconomyUT.pp.getAPI().take(player.getUniqueId(), (int) amount);
			return null;
		}
		final EconomyResponse trans = VaultHook.v_economy.withdrawPlayer(player, round(amount, 2));
		return trans;
	}
}
