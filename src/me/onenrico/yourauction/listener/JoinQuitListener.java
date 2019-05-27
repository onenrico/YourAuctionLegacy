//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.main.YourAuctionAPI;
import me.onenrico.yourauction.manager.PlaceholderManager;
import me.onenrico.yourauction.object.EExpiredItem;
import me.onenrico.yourauction.object.EPlayer;
import me.onenrico.yourauction.object.ESaleItem;
import me.onenrico.yourauction.object.ESoldItem;
import me.onenrico.yourauction.utils.MessageUT;

public class JoinQuitListener implements Listener {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@EventHandler
	public void join(final PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		final PlaceholderManager pm = new PlaceholderManager();
		EPlayer ep = YourAuctionAPI.getEPlayer(p.getUniqueId());
		if (ep.isAutotake()) {
			YourAuctionAPI.takeAll(p, p.getUniqueId());
		} else {
			final List<ESoldItem> esi = new ArrayList(ESoldItem.table.getOwned(p.getUniqueId()));
			for (final ESoldItem ee : new ArrayList<>(esi)) {
				if (ee.isTaken()) {
					esi.remove(ee);
				}
			}
			if (!esi.isEmpty()) {
				pm.add("sold", esi.size());
				new BukkitRunnable() {
					@Override
					public void run() {
						if (p.isOnline()) {
							MessageUT.plmessage(p, Locales.getValue("there_sold"), pm);
						}
					}
				}.runTaskLater(Core.getThis(), 20L);
			}
		}

		final List<ESaleItem> esai = new ArrayList(ESaleItem.table.getOwned(p.getUniqueId()));
		if (!esai.isEmpty()) {
			for (final ESaleItem ee2 : esai) {
				if (ee2.isExpired()) {
					EExpiredItem.create(ee2, true);
				}
			}
		}

		final List<EExpiredItem> eei = new ArrayList(EExpiredItem.table.getOwned(p.getUniqueId()));
		if (!eei.isEmpty()) {
			pm.add("amount", eei.size());
			new BukkitRunnable() {
				@Override
				public void run() {
					if (p.isOnline()) {
						MessageUT.plmessage(p, Locales.getValue("there_expired"), pm);
					}
				}
			}.runTaskLater(Core.getThis(), 20L);
		}
	}
}
