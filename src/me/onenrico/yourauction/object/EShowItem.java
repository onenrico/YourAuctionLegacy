package me.onenrico.yourauction.object;

import java.util.Comparator;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import me.onenrico.yourauction.manager.PlaceholderManager;

public interface EShowItem {
	public long getSaledate();

	public ItemStack getItem();

	public PlaceholderManager getPlaceholder();

	public boolean isExpired();

	public UUID getOwner();

	public boolean isSimilar(EShowItem es);

	public Comparator<EShowItem> comparator = new Comparator<EShowItem>() {
		@Override
		public int compare(final EShowItem o1, final EShowItem o2) {
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

}
