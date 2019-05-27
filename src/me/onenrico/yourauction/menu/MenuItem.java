package me.onenrico.yourauction.menu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import me.onenrico.yourauction.manager.PlaceholderManager;

public class MenuItem {
	private int slot;
	private ItemStack item;
	private Set<Action> actions;
	private PlaceholderManager pm;

	public MenuItem(final int slot, final ItemStack item) {
		actions = new HashSet<>();
		this.slot = slot;
		this.item = item;
	}

	public MenuItem(final int slot, final ItemStack item, final PlaceholderManager pm) {
		actions = new HashSet<>();
		this.slot = slot;
		this.item = item;
		this.pm = pm;
	}

	public int getSlot() {
		return slot;
	}

	public void setSlot(final int slot) {
		this.slot = slot;
	}

	public ItemStack getItem() {
		return item;
	}

	public void setItem(final ItemStack item) {
		this.item = item;
	}

	public MenuItem addAction(final Action action) {
		for (final Action act : new ArrayList<>(actions)) {
			if (act.valid(action.getClickType())) {
				actions.remove(act);
				actions.add(action);
				return this;
			}
		}
		actions.add(action);
		return this;
	}

	@Override
	public MenuItem clone() {
		final MenuItem result = new MenuItem(slot, item.clone(), pm);
		result.actions = new HashSet<>(actions);
		return result;
	}

	public void setActions(final Set<Action> actions) {
		this.actions = actions;
	}

	public Set<Action> getActions() {
		return actions;
	}

	public PlaceholderManager getPm() {
		return pm;
	}

	public void setPm(final PlaceholderManager pm) {
		this.pm = pm;
	}
}
