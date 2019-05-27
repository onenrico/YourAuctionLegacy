package me.onenrico.yourauction.menu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import me.onenrico.yourauction.config.EConfig;
import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.nbt.NBTItem;
import me.onenrico.yourauction.utils.ItemUT;
import me.onenrico.yourauction.utils.StringUT;

public class GUIMenu implements InventoryHolder {
	protected EConfig guiconfig;
	protected Inventory inv;
	protected UUID uuid;
	protected Set<MenuItem> menuitems;
	protected boolean editable;
	protected boolean stealable;
	protected String title;
	protected String name;
	protected UUID owner;
	protected int row;
	protected PlaceholderManager lastpm;
	protected GUIMenu before;
	protected OpenAnimation animation;
	protected Action closeaction;
	protected int stacksize;

	public static MyItem setupItem(final GUIMenu gm, final String guiitem, final int slot) {
		String name = "";
		if (gm != null) {
			name = gm.getName();
		}
		final String prefix = String.valueOf(name) + "." + guiitem + ".";
		String mat = Core.guiconfig.getStr(prefix + "Material", "NOTSET").toUpperCase()
				.replaceAll("SLIME_BALL_MINECART", "CHEST_MINECART");
		String display = Core.guiconfig.getStr(prefix + "Displayname", "&6" + guiitem + " &fName &cNot Configured !");
		List<String> lore = Core.guiconfig.getStrList(String.valueOf(prefix) + "Description",
				ItemUT.createLore("&6" + guiitem + " &fDescription &cNot Configured !"));
		int cslot = Core.guiconfig.getInt(prefix + "Slot", slot);
		ItemStack result = ItemUT.getItem(mat);
		display = Locales.pm.process(display);
		lore = Locales.pm.process(lore);
		result = ItemUT.changeDisplayName(result, display);
		result = ItemUT.changeLore(result, lore);
		return new MyItem(result, cslot);
	}

	public ItemStack secure(final ItemStack item) {
		return this.secure(item, true);
	}

	public ItemStack secure(ItemStack item, final Boolean forced) {
		if (item == null || item.getType().equals(Material.AIR)) {
			return item;
		}
		if (forced) {
			final NBTItem ni = new NBTItem(item);
			ni.setBoolean("nosteal", true);
			item = ni.getItem();
		}
		return item;
	}

	public static boolean isSecured(final ItemStack item) {
		final NBTItem ni = new NBTItem(item);
		return ni.hasKey("nosteal") && ni.getBoolean("nosteal");
	}

	public GUIMenu() {
		menuitems = new HashSet<>();
		row = 0;
		stacksize = 64;
		stealable = false;
		editable = false;
	}

	public GUIMenu(final String name, final String title, final int row) {
		this(name, title, row, false);
	}

	public GUIMenu(final String name, final String title, final int row, final UUID owner) {
		this(name, title, row, false, owner);
	}

	public GUIMenu(final String name, final String title, final int row, final boolean editable) {
		this(name, title, row, editable, false);
	}

	public GUIMenu(final String name, final String title, final int row, final boolean editable, final UUID owner) {
		this(name, title, row, editable, false, owner);
	}

	public GUIMenu(final String name, final String title, final int row, final boolean editable,
			final boolean stealable) {
		this(name, title, row, editable, stealable, null);
	}

	public GUIMenu(final String name, final String title, final int row, final boolean editable,
			final boolean stealable, final UUID owner) {
		this(name, title, row, editable, stealable, owner, null);
	}

	public GUIMenu(final String name, final String title, final int row, final boolean editable,
			final boolean stealable, final UUID owner, final GUIMenu before) {
		menuitems = new HashSet<>();
		this.row = 0;
		stacksize = 64;
		uuid = UUID.randomUUID();
		this.title = title;
		this.name = name;
		this.owner = owner;
		this.row = row;
		this.before = before;
		this.editable = editable;
		this.stealable = stealable;
		build();
	}

	public void build() {
		build(false);
	}

	public void build(boolean fresh) {
		if (title != null && title.length() > 32) {
			title = title.trim();
			if (title.length() > 32) {
				title = title.substring(0, 32);
			}
		}
		final Inventory tempinv = Bukkit.createInventory(this, row * 9, StringUT.t(title));
		tempinv.setMaxStackSize(stacksize);
		if (!fresh) {
			if (inv != null) {
				for (int x = 0; x < tempinv.getSize() && x < inv.getSize(); ++x) {
					tempinv.setItem(x, inv.getItem(x));
				}
			}
		}
		for (final MenuItem mi : new ArrayList<>(menuitems)) {
			if (mi.getSlot() + 1 >= tempinv.getSize()) {
				menuitems.remove(mi);
			}
		}
		inv = tempinv;
	}

	public MenuItem setItem(final int slot, final ItemStack item) {
		return this.setItem(slot, item, true);
	}

	public MenuItem setItem(final MyItem item) {
		return this.setItem(item.slot, item.item, true);
	}

	public MenuItem setItem(final int slot, final MyItem item) {
		return this.setItem(slot, item.item, true);
	}

	public MenuItem setItem(final MyItem item, final PlaceholderManager pm) {
		return this.setItem(item.slot, item.item, pm, true);
	}

	public MenuItem setItem(final int slot, final MyItem item, final PlaceholderManager pm) {
		return this.setItem(slot, item.item, pm, true);
	}

	public MenuItem setItem(final int slot, final ItemStack item, final PlaceholderManager pm) {
		return this.setItem(slot, item, pm, true);
	}

	public MenuItem changeItem(final int slot, final ItemStack item) {
		return this.setItem(slot, item, false);
	}

	public MenuItem changeItem(final int slot, final ItemStack item, final PlaceholderManager pm) {
		return this.setItem(slot, item, pm, false);
	}

	public MenuItem setItem(final int slot, ItemStack item, final boolean clear) {
		item = this.secure(item);
		MenuItem result = null;
		if (slot <= -1) {
			result = new MenuItem(-1, item);
			return result;
		}
		for (final MenuItem mi : menuitems) {
			if (mi.getSlot() == slot) {
				result = mi;
			}
		}
		if (result == null) {
			result = new MenuItem(slot, item);
			menuitems.add(result);
		} else {
			result.setItem(item);
		}
		if (clear) {
			result.getActions().clear();
		}
		inv.setItem(slot, item);
		return result;
	}

	public MenuItem setItem(final int slot, ItemStack item, final PlaceholderManager pm, final boolean clear) {
		item = this.secure(item);
		MenuItem result = null;
		if (slot <= -1) {
			result = new MenuItem(-1, item);
			return result;
		}
		for (final MenuItem mi : menuitems) {
			if (mi.getSlot() == slot) {
				result = mi;
				result.setPm(pm);
			}
		}
		if (result == null) {
			result = new MenuItem(slot, item, pm);
			menuitems.add(result);
		} else {
			result.setItem(item);
		}
		if (clear) {
			result.getActions().clear();
		}
		inv.setItem(slot, item);
		return result;
	}

	public MenuItem setItem(final int slot, final MenuItem mi) {
		for (final MenuItem ma : new ArrayList<>(menuitems)) {
			if (mi.getSlot() == ma.getSlot()) {
				menuitems.remove(ma);
			}
		}
		menuitems.add(mi);
		if (mi.getPm() != null) {
			inv.setItem(slot, mi.getPm().process(mi.getItem().clone()));
		} else {
			inv.setItem(slot, mi.getItem().clone());
		}
		return mi;
	}

	public void swapItem(final MenuItem item1, final MenuItem item2) {
		final int slot1 = item1.getSlot();
		item1.setSlot(item2.getSlot());
		item2.setSlot(slot1);
		inv.setItem(item1.getSlot(), item1.getItem());
		inv.setItem(item2.getSlot(), item2.getItem());
	}

	public void moveItem(final MenuItem item1, final int slot) {
		inv.setItem(item1.getSlot(), ItemUT.createItem(Material.AIR));
		menuitems.remove(item1);
		final MenuItem nitem = this.setItem(slot, item1.getItem());
		nitem.setActions(item1.getActions());
	}

	@Override
	public Inventory getInventory() {
		return getInv();
	}

	public MenuItem getMenuItem(final int slot) {
		for (final MenuItem mi : menuitems) {
			if (mi.getSlot() == slot) {
				return mi;
			}
		}
		return null;
	}

	public void refresh(PlaceholderManager pm) {
		if (pm == null) {
			pm = new PlaceholderManager();
			lastpm = pm;
		}
		for (final MenuItem mi : menuitems) {
			inv.setItem(mi.getSlot(), pm.process(mi.getItem().clone()));
		}
	}

	public void open(final Player player) {
		open(null, player);
	}

	public void open(Runnable callback, final Player player) {
		if (!ConfigManager.animation_enabled) {
			animation = null;
		}
		if (animation == null) {
			player.openInventory(inv);
			MenuLiveUpdate.refresh(this);
			player.updateInventory();
			MenuLiveUpdate.addAnimated(this);
			if (callback != null) {
				callback.run();
			}
		} else {
			animation.open(new Runnable() {
				@Override
				public void run() {
					player.updateInventory();
					MenuLiveUpdate.addAnimated(GUIMenu.this);
					if (callback != null) {
						callback.run();
					}
				}
			}, this, player);
		}
	}

	public Inventory getInv() {
		return inv;
	}

	public void setInv(final Inventory inv) {
		this.inv = inv;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(final UUID uuid) {
		this.uuid = uuid;
	}

	public Set<MenuItem> getMenuitems() {
		return menuitems;
	}

	public void setMenuitems(final Set<MenuItem> menuitems) {
		this.menuitems = menuitems;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setEditable(final boolean editable) {
		this.editable = editable;
	}

	public void setStealable(final boolean stealable) {
		this.stealable = stealable;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public boolean isStealable() {
		return stealable;
	}

	public boolean isEditable() {
		return editable;
	}

	public boolean hasOwner() {
		return owner != null;
	}

	public UUID getOwner() {
		return owner;
	}

	public void setOwner(final UUID owner) {
		this.owner = owner;
	}

	public GUIMenu getBefore() {
		return before;
	}

	public void setBefore(final GUIMenu before) {
		this.before = before;
	}

	public PlaceholderManager getLastpm() {
		return lastpm;
	}

	public void setLastpu(final PlaceholderManager lastpm) {
		this.lastpm = lastpm;
	}

	public OpenAnimation getAnimation() {
		return animation;
	}

	public void setAnimation(final OpenAnimation animation) {
		this.animation = animation;
	}

	public Action getCloseaction() {
		return closeaction;
	}

	public void setCloseaction(final Action closeaction) {
		this.closeaction = closeaction;
	}

	public int getRow() {
		return row;
	}

	public void setRow(final int row) {
		this.row = row;
	}
}
