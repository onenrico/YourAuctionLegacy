//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.object;

import java.util.HashMap;
import java.util.UUID;

import me.onenrico.yourauction.database.Datamanager;
import me.onenrico.yourauction.database.EObject;
import me.onenrico.yourauction.database.ETable;
import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.manager.ConfigManager;
import me.onenrico.yourauction.manager.PlaceholderManager;

public class EPlayer extends EObject {
	public static final ETable table = Datamanager.table_player_config;

	private String getValue(final String column) {
		return EPlayer.table.getValue(identifier, column);
	}

	private boolean autotake;

	public EPlayer(final String identifier) {
		super(identifier);
		super.table = EPlayer.table;
		owner = UUID.fromString(identifier);
		String v = getValue("Autotake");
		if (v.isEmpty() && ConfigManager.autocollect) {
			v = "1";
		}
		autotake = v.equals("1");
	}

	public static EPlayer create(final UUID owner) {
		final EPlayer result = new EPlayer(owner.toString());
		EPlayer.table.getLoadedData().add(result);
		EPlayer.table.addOwned(owner, result);
		return result;
	}

	@Override
	public HashMap<String, Object> getValues() {
		final HashMap<String, Object> result = new HashMap<>();
		result.put("Identifier", identifier);
		result.put("Autotake", autotake ? "1" : "0");
		return result;
	}

	public PlaceholderManager getPlaceholder() {
		final PlaceholderManager pm = new PlaceholderManager(Locales.pm);
		pm.add("autotake", autotake ? "True" : "False");
		return pm;
	}

	public boolean isAutotake() {
		return autotake;
	}

	public void setAutotake(boolean autotake) {
		this.autotake = autotake;
	}

}
