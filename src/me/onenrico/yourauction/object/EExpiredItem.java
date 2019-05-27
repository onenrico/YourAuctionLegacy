//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.object;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.text.StrLookup;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.onenrico.yourauction.database.Datamanager;
import me.onenrico.yourauction.database.EObject;
import me.onenrico.yourauction.database.ETable;
import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.utils.MathUT;
import me.onenrico.yourauction.utils.MessageUT;
import me.onenrico.yourauction.utils.TimeManager;

public class EExpiredItem extends EObject {
	public static Comparator<EExpiredItem> comparator;
	public static final ETable table;
	private ItemStack item;
	private long saledate;
	private long expirydate;
	private double price;

	static {
		EExpiredItem.comparator = new Comparator<EExpiredItem>() {
			@Override
			public int compare(final EExpiredItem o1, final EExpiredItem o2) {
				long total = o2.getSaledate() - o1.getSaledate();
				if (total < 0) {
					return -1;
				} else if (total > 0) {
					return 1;
				} else {
					return 0;
				}
			}
		};
		table = Datamanager.table_item_expired;
	}

	private String getValue(final String column) {
		return EExpiredItem.table.getValue(identifier, column);
	}

	public boolean isExpired() {
		if (expirydate == 0) {
			return false;
		}
		return expirydate - System.currentTimeMillis() <= 0L;
	}

	public EExpiredItem(final String identifier) {
		super(identifier);
		super.table = EExpiredItem.table;
		if (!EExpiredItem.table.getLoadedValue().containsKey(identifier)) {
			saledate = System.currentTimeMillis();
			if (ConfigManager.destroyHour < 0) {
				expirydate = 0;
			} else {
				expirydate = System.currentTimeMillis() + (long) (ConfigManager.destroyHour * 3600000.0);
			}
		} else {
			item = ItemSerializer.deserialize(getValue("Item"));
			if(item == null || item.getType().equals(Material.AIR)) {
				this.identifier = null;
				return;
			}
			owner = UUID.fromString(getValue("Seller"));
			saledate = Long.parseLong(getValue("Date"));
			expirydate = Long.parseLong(getValue("Expiry"));
			price = MathUT.safe(getValue("Price"));
		}
	}

	public static EExpiredItem create(final EShowItem from) {
		if (from instanceof ESaleItem) {
			return create((ESaleItem) from, false);
		} else if (from instanceof EBidItem) {
			return create((EBidItem) from, false);
		} else {
			return null;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static EExpiredItem create(final ESaleItem from, final boolean silent) {
		final EExpiredItem result = new EExpiredItem(UUID.randomUUID().toString());
		result.setOwner(from.getOwner());
		result.setItem(from.getItem());
		result.setPrice(from.getPrice());
		EExpiredItem.table.getLoadedData().add(result);
		EExpiredItem.table.addOwned(from.getOwner(), result);
		result.save();
		Datamanager.delete(from);
		if (!silent && Bukkit.getPlayer(from.getOwner()) != null) {
			final Player owner = Bukkit.getPlayer(from.getOwner());
			final List<EExpiredItem> essi = new ArrayList(EExpiredItem.table.getOwned(from.getOwner()));
			final PlaceholderManager pm = new PlaceholderManager();
			if (!essi.isEmpty()) {
				if ((pm.get("itemname").equals(""))) {
					pm.add("itemname", from.getItem().getType().toString());
				}
				pm.add("amount", essi.size());
				MessageUT.plmessage(owner, Locales.getValue("there_expired"), pm);
			}
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static EExpiredItem create(final EBidItem from, final boolean silent) {
		final EExpiredItem result = new EExpiredItem(UUID.randomUUID().toString());
		result.setOwner(from.getOwner());
		result.setItem(from.getItem());
		result.setPrice(from.getMaxbid());
		EExpiredItem.table.getLoadedData().add(result);
		EExpiredItem.table.addOwned(from.getOwner(), result);
		result.save();
		Datamanager.delete(from);
		if (!silent && Bukkit.getPlayer(from.getOwner()) != null) {
			final Player owner = Bukkit.getPlayer(from.getOwner());
			final List<EExpiredItem> essi = new ArrayList(EExpiredItem.table.getOwned(from.getOwner()));
			final PlaceholderManager pm = new PlaceholderManager();
			if (!essi.isEmpty()) {
				if ((pm.get("itemname").equals(""))) {
					pm.add("itemname", from.getItem().getType().toString());
				}
				pm.add("amount", essi.size());
				MessageUT.plmessage(owner, Locales.getValue("there_expired"), pm);
			}
		}
		return result;
	}

	@Override
	public HashMap<String, Object> getValues() {
		final HashMap<String, Object> result = new HashMap<>();
		result.put("Identifier", identifier);
		result.put("Item", ItemSerializer.serialize(item));
		result.put("Seller", owner.toString());
		result.put("Date", saledate);
		result.put("Expiry", expirydate);
		result.put("Price", price);
		return result;
	}

	public PlaceholderManager getPlaceholder() {
		final PlaceholderManager pm = new PlaceholderManager(Locales.pm);
		String name = "";
		if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
			name = item.getItemMeta().getDisplayName();
		}
		pm.add("displayname", name);
		pm.add("itemname", name);
		pm.add("price", MathUT.format(price));
		pm.add("seller", Bukkit.getOfflinePlayer(owner).getName());
		pm.add("expire", new StrLookup() {
			@Override
			public String lookup(String msg) {
				if (expirydate == 0) {
					return "N/A";
				}
				return TimeManager.formatTime(TimeManager.getSecond(System.currentTimeMillis(), expirydate));
			}
		});
		return pm;
	}

	public ItemStack getItem() {
		return item;
	}

	public void setItem(final ItemStack item) {
		this.item = item;
	}

	public long getSaledate() {
		return saledate;
	}

	public void setSaledate(final long saledate) {
		this.saledate = saledate;
	}

	public long getExpirydate() {
		return expirydate;
	}

	public void setExpirydate(final long expirydate) {
		this.expirydate = expirydate;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(final double price) {
		this.price = price;
	}
}
