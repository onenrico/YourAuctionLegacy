//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import me.onenrico.yourauction.database.Datamanager;
import me.onenrico.yourauction.database.EObject;
import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.object.EBidItem;
import me.onenrico.yourauction.object.EExpiredItem;
import me.onenrico.yourauction.object.EPlayer;
import me.onenrico.yourauction.object.ESaleItem;
import me.onenrico.yourauction.object.EShowItem;
import me.onenrico.yourauction.object.ESoldItem;
import me.onenrico.yourauction.utils.EconomyUT;
import me.onenrico.yourauction.utils.MathUT;
import me.onenrico.yourauction.utils.MessageUT;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class YourAuctionAPI {
	public static int getMaxSale(final OfflinePlayer ofp) {
		int highest = 0;
		if (ofp.isOnline()) {
			final Player p = ofp.getPlayer();
			for (final PermissionAttachmentInfo pai : p.getEffectivePermissions()) {
				String perm = pai.getPermission();
				if (perm.startsWith("ya.maxsell.")) {
					perm = perm.replace("ya.maxsell.", "");
					if (perm.equalsIgnoreCase("*")) {
						return 1000;
					}
					final int amount = MathUT.strInt(perm);
					if (amount <= highest) {
						continue;
					}
					highest = amount;
				}
			}
			if (highest > 0) {
				return highest;
			}
		}
		return ConfigManager.defaultmaxsell;
	}

	public static List<ESaleItem> getSaleItems() {
		final List<ESaleItem> sorted = new ArrayList(ESaleItem.table.getLoadedData());
		final Set<ESaleItem> expired = new HashSet<>();
		for (final EObject eo : new ArrayList<EObject>(sorted)) {
			final ESaleItem es = (ESaleItem) eo;
			if (es.isExpired()) {
				expired.add(es);
				sorted.remove(es);
			}
		}
		int i = 0;
		for (final ESaleItem ex : expired) {
			if (i + 1 == expired.size()) {
				EExpiredItem.create(ex);
				break;
			}
			EExpiredItem.create(ex, true);
			++i;
		}
		sorted.sort(EShowItem.comparator);
		return sorted;
	}

	public static Set<UUID> getSaleItemsOwners() {
		final Set<UUID> owners = ESaleItem.table.getOwnedObject().keySet();
		return owners;
	}

	public static HashMap<UUID, List<ESaleItem>> getDetailSaleItems() {
		final HashMap<UUID, List<ESaleItem>> result = new HashMap<>();
		for (UUID uuid : getSaleItemsOwners()) {
			result.put(uuid, getSaleItems(uuid));
		}
		return result;
	}

	public static List<ESaleItem> getSaleItems(final UUID owner) {
		final List<ESaleItem> sorted = new ArrayList(ESaleItem.table.getOwned(owner));
		final Set<ESaleItem> expired = new HashSet<>();
		for (final EObject eo : new ArrayList<EObject>(sorted)) {
			final ESaleItem es = (ESaleItem) eo;
			if (es.isExpired()) {
				expired.add(es);
				sorted.remove(es);
			}
		}
		int i = 0;
		for (final ESaleItem ex : expired) {
			if (i + 1 == expired.size()) {
				EExpiredItem.create(ex);
				break;
			}
			EExpiredItem.create(ex, true);
			++i;
		}
		sorted.sort(EShowItem.comparator);
		return sorted;
	}

	public static List<EBidItem> getBidItems() {
		final List<EBidItem> sorted = new ArrayList(EBidItem.table.getLoadedData());
		final Set<EBidItem> expired = new HashSet<>();
		for (final EObject eo : new ArrayList<EObject>(sorted)) {
			final EBidItem es = (EBidItem) eo;
			if (es.isExpired()) {
				expired.add(es);
				sorted.remove(es);
			}
		}
		int i = 0;
		for (final EBidItem ex : expired) {
			if (i + 1 == expired.size()) {
				EExpiredItem.create(ex);
				break;
			}
			EExpiredItem.create(ex, true);
			++i;
		}
		sorted.sort(EShowItem.comparator);
		return sorted;
	}

	public static List<EBidItem> getBidItems(final UUID owner) {
		final List<EBidItem> sorted = new ArrayList(EBidItem.table.getOwned(owner));
		final Set<EBidItem> expired = new HashSet<>();
		for (final EObject eo : new ArrayList<EObject>(sorted)) {
			final EBidItem es = (EBidItem) eo;
			if (es.isExpired()) {
				expired.add(es);
				sorted.remove(es);
			}
		}
		int i = 0;
		for (final EBidItem ex : expired) {
			if (i + 1 == expired.size()) {
				EExpiredItem.create(ex);
				break;
			}
			EExpiredItem.create(ex, true);
			++i;
		}
		sorted.sort(EShowItem.comparator);
		return sorted;
	}

	public static List<EShowItem> getShowItems() {
		final List<EShowItem> sorted = new ArrayList();
		sorted.addAll(getSaleItems());
		sorted.addAll(getBidItems());
		sorted.sort(EShowItem.comparator);
		if (ConfigManager.compact) {
			HashMap<UUID, Set<EShowItem>> cacheShow = new HashMap<>();
			for (EShowItem esi : new ArrayList<>(sorted)) {
				Set<EShowItem> in = cacheShow.getOrDefault(esi.getOwner(), new HashSet<>());
				for (EShowItem esa : in) {
					if (esi.isSimilar(esa)) {
						sorted.remove(esi);
						continue;
					}
				}
				in.add(esi);
				cacheShow.put(esi.getOwner(), in);
			}
		}
		return sorted;
	}

	public static List<EShowItem> getShowItems(final UUID owner) {
		final List<EShowItem> sorted = new ArrayList();
		sorted.addAll(getSaleItems(owner));
		sorted.addAll(getBidItems(owner));
		sorted.sort(EShowItem.comparator);
		if (ConfigManager.compact) {
			HashMap<UUID, Set<EShowItem>> cacheShow = new HashMap<>();
			for (EShowItem esi : new ArrayList<>(sorted)) {
				Set<EShowItem> in = cacheShow.getOrDefault(esi.getOwner(), new HashSet<>());
				for (EShowItem esa : in) {
					if (esi.isSimilar(esa)) {
						sorted.remove(esi);
						continue;
					}
				}
				in.add(esi);
				cacheShow.put(esi.getOwner(), in);
			}
		}
		return sorted;
	}

	public static List<EExpiredItem> getExpiredItems(final UUID player) {
		final List<EExpiredItem> sorted = new ArrayList(EExpiredItem.table.getOwned(player));
		for (final EObject eo : new ArrayList<EObject>(sorted)) {
			final EExpiredItem es = (EExpiredItem) eo;
			if (es.isExpired()) {
				Datamanager.delete(es);
				sorted.remove(es);
			}
		}
		sorted.sort(EExpiredItem.comparator);
		return sorted;
	}

	public static EPlayer getEPlayer(final UUID player) {
		List<EPlayer> eps = new ArrayList(EPlayer.table.getOwned(player));
		if (eps.size() > 0) {
			return eps.get(0);
		} else {
			return EPlayer.create(player);
		}
	}

	public static void takeAll(Player player, UUID target) {
		double total = 0;
		for (ESoldItem es : getSoldItems(target)) {
			if (!es.isTaken()) {
				total += es.getTotalmoney();
				es.setTaken(true);
				es.save();
			}
		}
		if (total > 0) {
			PlaceholderManager pm = new PlaceholderManager();
			pm.add("total", total);
			EconomyUT.addBal(player, total);
			MessageUT.plmessage(player, Locales.getValue("success_take_sold"), pm);
		}
	}

	public static List<ESoldItem> getSoldItems(final UUID player) {
		final List<ESoldItem> sorted = new ArrayList(ESoldItem.table.getOwned(player));
		sorted.sort(ESoldItem.comparator);
		return sorted;
	}
}
