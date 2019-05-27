//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.database;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import me.onenrico.yourauction.main.Core;

public abstract class EObject {
	protected String identifier;
	protected UUID owner;
	protected ETable table;

	public abstract HashMap<String, Object> getValues();

	public EObject(final String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public UUID getOwner() {
		return owner;
	}

	public ETable getTable() {
		return table;
	}

	public void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}

	public void setOwner(final UUID owner) {
		this.owner = owner;
	}

	public void setTable(final ETable table) {
		this.table = table;
	}

	public void save() {
		new BukkitRunnable() {
			@Override
			public void run() {
				Datamanager.save(EObject.this);
			}
		}.runTask(Core.getThis());
	}
}
