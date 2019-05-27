//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.utils.EMaterial;
import me.onenrico.yourauction.utils.MessageUT;

public class SmileAnimation extends OpenAnimation {
	@Override
	public void open(final Runnable callback, final GUIMenu gm, final Player p) {
		if (p.isOnline()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					final Inventory inv = gm.getInventory();
					int row = inv.getSize() / 9;
					Integer[] pattern1 = { 12, 14, 38, 42, 30, 31, 32 };
					Integer[] pattern2 = { 12, 14, 38, 42, 39, 40, 41 };
					Integer[] pattern3 = { 12, 14, 29, 33, 39, 40, 41 };
					EMaterial[] smiles = { EMaterial.RED_STAINED_GLASS_PANE, EMaterial.WHITE_STAINED_GLASS_PANE, 
							EMaterial.LIME_STAINED_GLASS_PANE};
					List<Integer[]> patterns = new ArrayList<>();
					patterns.add(pattern1);
					patterns.add(pattern2);
					patterns.add(pattern3);
					p.openInventory(inv);
					new BukkitRunnable() {
						int cleanerslot = 0;
						boolean smilereverse = false;
						int smilestate = 0;
						boolean first = true;
						boolean cleanerreverse = false;
						UUID uuid = UUID.randomUUID();
						@Override
						public void run() {
							MessageUT.cmsg("From:"+uuid);
							if(first) {
								for(int i = 0;i < inv.getSize();i++) {
									inv.setItem(i, EMaterial.BLACK_STAINED_GLASS_PANE.parseItem());
								}
								first = false;
								return;
							}
							else{
								Integer[] pattern = patterns.get(smilestate);
								for (int r = 0; r < row; r++) {
									int cslot = cleanerslot + (r * 9);
									int prevslot = cleanerreverse ? cslot + 1 : cslot - 1;
									if (prevslot >= (r * 9)) {
										boolean found = false;
										for(int p : pattern) {
											if(prevslot == p) {
												found = true;
												break;
											}
										}
										if(!found) {
											inv.setItem(prevslot, EMaterial.BLACK_STAINED_GLASS_PANE.parseItem());
										}
									}
									boolean found = false;
									for(int p : pattern) {
										if(cslot == p) {
											inv.setItem(cslot, smiles[smilestate].parseItem());
											found = true;
											break;
										}
									}
									if(!found) {
										inv.setItem(cslot, EMaterial.BLACK_STAINED_GLASS_PANE.parseItem());
									}
									int bound = cleanerreverse ? (r * 9) : (r * 9)+8;
									
									if (r + 1 == row && cslot == bound) {
										cleanerreverse = !cleanerreverse;
										smilestate = smilereverse ? smilestate-1 : smilestate+1;
										if(smilestate == -1) {
											smilestate = 1;
											smilereverse = false;
										}else if(smilestate == patterns.size()) {
											smilestate = 1;
											smilereverse = true;
										}
									}
								}
								cleanerslot = cleanerreverse ? cleanerslot-1 : cleanerslot+1;
							}
						}
					}.runTaskTimer(Core.getThis(), 0, 1);
				}
			}.runTask(Core.getThis());
		}
	}
}
