package me.onenrico.yourauction.menu;

import org.bukkit.inventory.ItemStack;

public class MyItem {
	public int slot;
	public ItemStack item;

	public MyItem(ItemStack item, int slot) {
		this.item = item;
		this.slot = slot;
	}

	@Override
	public ItemStack clone() {
		return item.clone();
	}

	public MyItem cloneThis() {
		return new MyItem(clone(), slot);
	}
}
