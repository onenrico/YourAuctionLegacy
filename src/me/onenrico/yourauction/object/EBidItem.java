//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.object;

import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang.text.StrLookup;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import me.onenrico.yourauction.database.Datamanager;
import me.onenrico.yourauction.database.EObject;
import me.onenrico.yourauction.database.ETable;
import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.utils.MathUT;
import me.onenrico.yourauction.utils.TimeManager;

public class EBidItem extends EObject implements EShowItem {
	public static final ETable table = Datamanager.table_item_bid;
	private ItemStack item;
	private long saledate;
	private long expirydate;
	private double currentbid;
	private double maxbid;
	private String bidder;

	private String getValue(final String column) {
		return EBidItem.table.getValue(identifier, column);
	}

	@Override
	public boolean isExpired() {
		if (expirydate == 0) {
			return false;
		}
		return expirydate - System.currentTimeMillis() <= 0L;
	}

	public EBidItem(final String identifier) {
		super(identifier);
		super.table = EBidItem.table;
		if (!EBidItem.table.getLoadedValue().containsKey(identifier)) {
			saledate = System.currentTimeMillis();
			if (ConfigManager.saleHour < 0) {
				expirydate = 0;
			} else {
				expirydate = System.currentTimeMillis() + (long) (ConfigManager.saleHour * 3600000.0);
			}
		} else {
			item = ItemSerializer.deserialize(getValue("Item"));
			owner = UUID.fromString(getValue("Seller"));
			saledate = Long.parseLong(getValue("Date"));
			expirydate = Long.parseLong(getValue("Expiry"));
			String biduuid = getValue("Bidder");
			if (!biduuid.isEmpty()) {
				bidder = Bukkit.getOfflinePlayer(UUID.fromString(biduuid)).getName();
			}
			currentbid = MathUT.safe(getValue("Currentbid"));
			maxbid = MathUT.safe(getValue("Maxbid"));
		}
	}

	public static EBidItem create(final UUID owner, final ItemStack item, final double price) {
		final EBidItem result = new EBidItem(UUID.randomUUID().toString());
		result.setOwner(owner);
		result.setItem(item);
		result.setMaxbid(price);
		result.setCurrentbid(0);
		result.setBidder("");
		EBidItem.table.getLoadedData().add(result);
		EBidItem.table.addOwned(owner, result);
		return result;
	}

	@SuppressWarnings("deprecation")
	@Override
	public HashMap<String, Object> getValues() {
		final HashMap<String, Object> result = new HashMap<>();
		result.put("Identifier", identifier);
		result.put("Item", ItemSerializer.serialize(item));
		result.put("Seller", owner.toString());
		result.put("Date", saledate);
		result.put("Expiry", expirydate);
		result.put("Currentbid", currentbid);
		result.put("Maxbid", maxbid);
		result.put("Bidder", Bukkit.getOfflinePlayer(bidder).getUniqueId());
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
		pm.add("currentbid", MathUT.format(currentbid));
		pm.add("maxbid", MathUT.format(maxbid));
		pm.add("bidder", bidder);
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

	public double getCurrentbid() {
		return currentbid;
	}

	public void setCurrentbid(double currentbid) {
		this.currentbid = currentbid;
	}

	public double getMaxbid() {
		return maxbid;
	}

	public void setMaxbid(double maxbid) {
		this.maxbid = maxbid;
	}

	public String getBidder() {
		return bidder;
	}

	public void setBidder(String bidder) {
		this.bidder = bidder;
	}

	@Override
	public boolean isSimilar(EShowItem es) {
		// TODO Auto-generated method stub
		return false;
	}

}
