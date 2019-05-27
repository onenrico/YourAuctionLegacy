//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.object;

import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang.text.StrLookup;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.onenrico.yourauction.database.Datamanager;
import me.onenrico.yourauction.database.EObject;
import me.onenrico.yourauction.database.ETable;
import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.utils.MathUT;
import me.onenrico.yourauction.utils.TimeManager;

public class ESaleItem extends EObject implements EShowItem {
	public static final ETable table = Datamanager.table_item_sale;
	private ItemStack item;
	private long saledate;
	private long expirydate;
	private double price;

	private String getValue(final String column) {
		return ESaleItem.table.getValue(identifier, column);
	}

	@Override
	public boolean isExpired() {
		if (expirydate == 0) {
			return false;
		}
		return expirydate - System.currentTimeMillis() <= 0L;
	}

	public ESaleItem(final String identifier) {
		super(identifier);
		super.table = ESaleItem.table;
		if (!ESaleItem.table.getLoadedValue().containsKey(identifier)) {
			saledate = System.currentTimeMillis();
			if (ConfigManager.saleHour < 0) {
				expirydate = 0;
			} else {
				expirydate = System.currentTimeMillis() + (long) (ConfigManager.saleHour * 3600000.0);
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

	public static ESaleItem create(final UUID owner, final ItemStack item, final double price) {
		final ESaleItem result = new ESaleItem(UUID.randomUUID().toString());
		result.setOwner(owner);
		result.setItem(item);
		result.setPrice(price);
		ESaleItem.table.getLoadedData().add(result);
		ESaleItem.table.addOwned(owner, result);
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

	@Override
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

	@Override
	public ItemStack getItem() {
		return item;
	}

	public void setItem(final ItemStack item) {
		this.item = item;
	}

	@Override
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

	@Override
	public boolean isSimilar(EShowItem es) {
		ESaleItem esi = (ESaleItem) es;
		if (esi.getPrice() == getPrice()) {
			if (esi.getItem().getAmount() == getItem().getAmount()) {
				if (esi.getItem().isSimilar(getItem())) {
					return true;
				}
			}
		}
		return false;
	}
}
