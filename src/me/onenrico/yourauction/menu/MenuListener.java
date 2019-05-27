//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.menu;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.onenrico.yourauction.gui.SellallMenu;
import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.utils.MessageUT;

public class MenuListener implements Listener {
	@EventHandler
	public void onDrag(final InventoryDragEvent event) {
		final Inventory top = event.getView().getTopInventory();
		if (top != null && top.getHolder() instanceof GUIMenu && event.getWhoClicked() instanceof Player) {
			final GUIMenu gm = (GUIMenu) top.getHolder();
			if (gm instanceof SellallMenu) {
				((SellallMenu) gm).check((Player) event.getWhoClicked());
			}
			if (gm.isEditable()) {
				return;
			}
			final Inventory inv = event.getInventory();
			for (final int slot : event.getRawSlots()) {
				if (slot < inv.getSize()) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onClick(final InventoryClickEvent e) {
		if (e.getSlotType().equals(InventoryType.SlotType.OUTSIDE) && e.getClickedInventory() == null) {
			return;
		}
		final Inventory top = e.getView().getTopInventory();
		if (top == null || !(top.getHolder() instanceof GUIMenu)) {
			return;
		}
		final GUIMenu gm = (GUIMenu) top.getHolder();
		if (gm instanceof SellallMenu) {
			((SellallMenu) gm).check((Player) e.getWhoClicked());
		}
		e.setCancelled(true);
		if (e.getClickedInventory().equals(e.getView().getBottomInventory())) {
			if (gm.isEditable()) {
				e.setCancelled(false);
			}
			return;
		}
		final int slot = e.getSlot();
		final MenuItem mi = gm.getMenuItem(slot);
		if (mi == null) {
			if (gm.isStealable()) {
				e.setCancelled(false);
			}
			return;
		}
		MessageUT.plmessage((Player) e.getWhoClicked(), Locales.getValue("gui_click"));
		for (Action act : new ArrayList<>(mi.getActions())) {
			if (act.valid(e.getClick())) {
				act.run();
			}
		}
	}

	@EventHandler
	public void onDrop(final PlayerDropItemEvent e) {
		if (GUIMenu.isSecured(e.getItemDrop().getItemStack())) {
			e.getItemDrop().remove();
		}
	}

	@EventHandler
	public void onClose(final InventoryCloseEvent e) {
		final Inventory inv = e.getInventory();
		if (inv != null && inv.getHolder() instanceof GUIMenu) {
			final GUIMenu gm = (GUIMenu) inv.getHolder();
			if (gm.getCloseaction() != null) {
				gm.getCloseaction().run();
			}
			if (gm.getBefore() != null) {
				gm.getBefore().open((Player) e.getPlayer());
			}
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				final Player p = (Player) e.getPlayer();
				if (!p.isOnline()) {
					return;
				}
				final ItemStack[] inven = p.getInventory().getContents();
				if (inven.length < 1) {
					return;
				}
				ItemStack[] array;
				for (int length = (array = inven).length, j = 0; j < length; ++j) {
					final ItemStack i = array[j];
					if (!p.isOnline()) {
						return;
					}
					if (i != null && GUIMenu.isSecured(i)) {
						e.getPlayer().getInventory().remove(i);
					}
				}
			}
		}.runTask(Core.getThis());
	}
}
