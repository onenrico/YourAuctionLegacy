//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.object;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.onenrico.yourauction.database.Datamanager;
import me.onenrico.yourauction.database.EObject;
import me.onenrico.yourauction.database.ETable;
import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.main.YourAuctionAPI;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.utils.EconomyUT;
import me.onenrico.yourauction.utils.MathUT;
import me.onenrico.yourauction.utils.MessageUT;
import me.onenrico.yourauction.utils.TimeManager;

public class ESoldItem extends EObject {
	public static Comparator<ESoldItem> comparator;
	public static final ETable table;
	private ItemStack item;
	private UUID buyer;
	private long solddate;
	private double price;
	private double tax;
	private double totalmoney;
	private boolean taken;

	static {
		ESoldItem.comparator = new Comparator<ESoldItem>() {
			@Override
			public int compare(final ESoldItem o1, final ESoldItem o2) {
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
		table = Datamanager.table_item_sold;
	}

	private String getValue(final String column) {
		return ESoldItem.table.getValue(identifier, column);
	}

	public static void refresh(final ESoldItem es) {
		es.totalmoney = es.price - es.price * es.tax / 100.0;
	}

	public ESoldItem(final String identifier) {
		super(identifier);
		super.table = ESoldItem.table;
		if (!ESoldItem.table.getLoadedValue().containsKey(identifier)) {
			solddate = System.currentTimeMillis();
		} else {
			item = ItemSerializer.deserialize(getValue("Item"));
			if(item == null || item.getType().equals(Material.AIR)) {
				this.identifier = null;
				return;
			}
			owner = UUID.fromString(getValue("Seller"));
			buyer = UUID.fromString(getValue("Buyer"));
			solddate = Long.parseLong(getValue("Date"));
			price = MathUT.safe(getValue("Price"));
			tax = MathUT.safe(getValue("Tax"));
			taken = getValue("Taken").equals("1");
		}
		refresh(this);
	}

	public static ESoldItem create(final EShowItem from, final UUID buyer) {
		if (from instanceof ESaleItem) {
			return create((ESaleItem) from, buyer);
		}
		return create((EBidItem) from, buyer);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ESoldItem create(final ESaleItem from, final UUID buyer) {
		final ESoldItem result = new ESoldItem(UUID.randomUUID().toString());
		result.setOwner(from.getOwner());
		result.setItem(from.getItem());
		result.setPrice(from.getPrice());
		result.setBuyer(buyer);
		result.setTax(ConfigManager.tax);
		EPlayer ep = YourAuctionAPI.getEPlayer(from.getOwner());
		if (ep.isAutotake() && ConfigManager.instantautocollect) {
			result.setTaken(true);
		} else {
			result.setTaken(false);
		}
		refresh(result);
		ESoldItem.table.getLoadedData().add(result);
		ESoldItem.table.addOwned(from.getOwner(), result);
		Datamanager.delete(from);
		result.save();
		if (ep.isAutotake() && ConfigManager.instantautocollect) {
			EconomyUT.addBal(from.getOwner(), result.getTotalmoney());
		}
		if (Bukkit.getPlayer(from.getOwner()) != null) {
			final Player owner = Bukkit.getPlayer(from.getOwner());
			if (ep.isAutotake() && ConfigManager.instantautocollect) {
				PlaceholderManager pm = new PlaceholderManager();
				pm.add("total", result.getTotalmoney());
				MessageUT.plmessage(owner, Locales.getValue("success_take_sold"), pm);
			}
			final List<ESoldItem> essi = new ArrayList(ESoldItem.table.getOwned(from.getOwner()));
			final PlaceholderManager pm = new PlaceholderManager();
			for (final ESoldItem ee : new ArrayList<>(essi)) {
				if (ee.isTaken()) {
					essi.remove(ee);
				}
			}
			if (!essi.isEmpty()) {
				if ((pm.get("itemname").equals(""))) {
					pm.add("itemname", from.getItem().getType().toString());
				}
				pm.add("sold", essi.size());
				MessageUT.plmessage(owner, Locales.getValue("there_sold"), pm);
			}
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ESoldItem create(final EBidItem from, final UUID buyer) {
		final ESoldItem result = new ESoldItem(UUID.randomUUID().toString());
		result.setOwner(from.getOwner());
		result.setItem(from.getItem());
		result.setPrice(from.getCurrentbid());
		result.setBuyer(buyer);
		result.setTax(ConfigManager.tax);
		EPlayer ep = YourAuctionAPI.getEPlayer(from.getOwner());
		if (ep.isAutotake() && ConfigManager.instantautocollect) {
			result.setTaken(true);
		} else {
			result.setTaken(false);
		}
		refresh(result);
		ESoldItem.table.getLoadedData().add(result);
		ESoldItem.table.addOwned(from.getOwner(), result);
		Datamanager.delete(from);
		result.save();
		if (ep.isAutotake() && ConfigManager.instantautocollect) {
			EconomyUT.addBal(from.getOwner(), result.getTotalmoney());
		}
		if (Bukkit.getPlayer(from.getOwner()) != null) {
			final Player owner = Bukkit.getPlayer(from.getOwner());
			if (ep.isAutotake() && ConfigManager.instantautocollect) {
				PlaceholderManager pm = new PlaceholderManager();
				pm.add("total", result.getTotalmoney());
				MessageUT.plmessage(owner, Locales.getValue("success_take_sold"), pm);
			}
			final List<ESoldItem> essi = new ArrayList(ESoldItem.table.getOwned(from.getOwner()));
			final PlaceholderManager pm = new PlaceholderManager();
			for (final ESoldItem ee : new ArrayList<>(essi)) {
				if (ee.isTaken()) {
					essi.remove(ee);
				}
			}
			if (!essi.isEmpty()) {
				if ((pm.get("itemname").equals(""))) {
					pm.add("itemname", from.getItem().getType().toString());
				}
				pm.add("sold", essi.size());
				MessageUT.plmessage(owner, Locales.getValue("there_sold"), pm);
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
		result.put("Buyer", buyer.toString());
		result.put("Date", solddate);
		result.put("Price", price);
		result.put("Tax", tax);
		result.put("Taken", taken ? "1" : "0");
		return result;
	}

	public PlaceholderManager getPlaceholder() {
		final PlaceholderManager pu = new PlaceholderManager(Locales.pm);
		String name = "";
		if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
			name = item.getItemMeta().getDisplayName();
		}
		pu.add("displayname", name);
		pu.add("itemname", name);
		pu.add("price", MathUT.format(price));
		pu.add("tax", String.valueOf(tax) + "%");
		pu.add("total", MathUT.format(totalmoney));
		pu.add("seller", Bukkit.getOfflinePlayer(owner).getName());
		pu.add("buyer", Bukkit.getOfflinePlayer(buyer).getName());
		final String sd = TimeManager.toString(TimeManager.fromLong(solddate));
		pu.add("date", sd.substring(0, sd.length() - 4));
		return pu;
	}

	public ItemStack getItem() {
		return item;
	}

	public void setItem(final ItemStack item) {
		this.item = item;
	}

	public long getSaledate() {
		return solddate;
	}

	public void setSaledate(final long saledate) {
		solddate = saledate;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(final double price) {
		this.price = price;
	}

	public UUID getBuyer() {
		return buyer;
	}

	public void setBuyer(final UUID buyer) {
		this.buyer = buyer;
	}

	public long getSolddate() {
		return solddate;
	}

	public void setSolddate(final long solddate) {
		this.solddate = solddate;
	}

	public double getTax() {
		return tax;
	}

	public void setTax(final double tax) {
		this.tax = tax;
	}

	public double getTotalmoney() {
		return totalmoney;
	}

	public void setTotalmoney(final double totalmoney) {
		this.totalmoney = totalmoney;
	}

	public boolean isTaken() {
		return taken;
	}

	public void setTaken(final boolean taken) {
		this.taken = taken;
	}
}
