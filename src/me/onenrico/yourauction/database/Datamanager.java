//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import me.onenrico.yourauction.database.sql.Database;
import me.onenrico.yourauction.database.sql.MySQL;
import me.onenrico.yourauction.database.sql.SQLite;
import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.object.EBidItem;
import me.onenrico.yourauction.object.EExpiredItem;
import me.onenrico.yourauction.object.EPlayer;
import me.onenrico.yourauction.object.ESaleItem;
import me.onenrico.yourauction.object.ESoldItem;
import me.onenrico.yourauction.utils.MessageUT;
import me.onenrico.yourauction.utils.SqlUT;

public class Datamanager {
	public static Database db;
	public static ETable table_item_sale;
	public static ETable table_item_bid;
	public static ETable table_item_expired;
	public static ETable table_item_sold;
	public static ETable table_player_config;
	public static Found databasetype;

	public static void setup() {
		final String pref = "database.";
		Datamanager.databasetype = Found.getType(Core.databaseconfig.getStr(String.valueOf(pref) + "type", "sqlite"));
		if (Datamanager.databasetype.equals(Found.MYSQL)) {
			Datamanager.databasetype = Found.SQLITE;
		}
		switch (Datamanager.databasetype) {
		case MYSQL: {
			final String hostname = Core.databaseconfig.getStr(String.valueOf(pref) + "hostname", "unknown");
			if (hostname.equalsIgnoreCase("unknown")) {
				MessageUT.cmsg("You are using MySQL but credential not set correctly on database.yml");
				Core.getThis().getPluginLoader().disablePlugin(Core.getThis());
				return;
			}
			final String port = Core.databaseconfig.getStr(String.valueOf(pref) + "port", "thereisnothinghere");
			final String database = Core.databaseconfig.getStr(String.valueOf(pref) + "database", "thereisnothinghere");
			final String username = Core.databaseconfig.getStr(String.valueOf(pref) + "user", "thereisnothinghere");
			final String password = Core.databaseconfig.getStr(String.valueOf(pref) + "password", "thereisnothinghere");
			Datamanager.db = new MySQL(hostname, port, database, username, password);
			break;
		}
		case SQLITE: {
			Datamanager.db = new SQLite();
			break;
		}
		default:
			break;
		}
		MessageUT.cmsg("Connecting to " + Datamanager.databasetype + " Database...");
		if (Datamanager.db != null) {
			Datamanager.db.load();
		}
		MessageUT.cmsg("Connected to " + Datamanager.databasetype);
		setupTable();
	}

	public static Database getDB() {
		return Datamanager.db;
	}

	public static void setupTable() {
		Datamanager.table_item_sale = new ETable("YOURAUCTION_ITEM_SALE", ESaleItem.class);
		Datamanager.table_item_bid = new ETable("YOURAUCTION_ITEM_BID", EBidItem.class);
		Datamanager.table_item_expired = new ETable("YOURAUCTION_ITEM_EXPIRED", EExpiredItem.class);
		Datamanager.table_item_sold = new ETable("YOURAUCTION_ITEM_SOLD", ESoldItem.class);
		Datamanager.table_player_config = new ETable("YOURAUCTION_PLAYER_CONFIG", EPlayer.class);
		Datamanager.table_item_sale.setPrimaryKey("Identifier");
		Datamanager.table_item_sale.addColumn("Identifier", "char(36)");
		Datamanager.table_item_sale.addColumn("Item", "text");
		Datamanager.table_item_sale.addColumn("Seller", "char(36)");
		Datamanager.table_item_sale.addColumn("Date", "bigint");
		Datamanager.table_item_sale.addColumn("Expiry", "bigint");
		Datamanager.table_item_sale.addColumn("Price", "double");
		Datamanager.table_item_bid.setPrimaryKey("Identifier");
		Datamanager.table_item_bid.addColumn("Identifier", "char(36)");
		Datamanager.table_item_bid.addColumn("Item", "text");
		Datamanager.table_item_bid.addColumn("Seller", "char(36)");
		Datamanager.table_item_bid.addColumn("Date", "bigint");
		Datamanager.table_item_bid.addColumn("Expiry", "bigint");
		Datamanager.table_item_bid.addColumn("Currentbid", "double");
		Datamanager.table_item_bid.addColumn("Maxbid", "double");
		Datamanager.table_item_bid.addColumn("Bidder", "varchar(36)");
		Datamanager.table_item_expired.setPrimaryKey("Identifier");
		Datamanager.table_item_expired.addColumn("Identifier", "char(36)");
		Datamanager.table_item_expired.addColumn("Item", "text");
		Datamanager.table_item_expired.addColumn("Seller", "char(36)");
		Datamanager.table_item_expired.addColumn("Date", "bigint");
		Datamanager.table_item_expired.addColumn("Expiry", "bigint");
		Datamanager.table_item_expired.addColumn("Price", "double");
		Datamanager.table_item_sold.setPrimaryKey("Identifier");
		Datamanager.table_item_sold.addColumn("Identifier", "char(36)");
		Datamanager.table_item_sold.addColumn("Item", "text");
		Datamanager.table_item_sold.addColumn("Seller", "char(36)");
		Datamanager.table_item_sold.addColumn("Buyer", "char(36)");
		Datamanager.table_item_sold.addColumn("Date", "bigint");
		Datamanager.table_item_sold.addColumn("Price", "double");
		Datamanager.table_item_sold.addColumn("Tax", "double");
		Datamanager.table_item_sold.addColumn("Taken", "tinyint(1)");
		Datamanager.table_player_config.setPrimaryKey("Identifier");
		Datamanager.table_player_config.addColumn("Identifier", "char(36)");
		Datamanager.table_player_config.addColumn("Autotake", "tinyint(1)");
		final long beginning = System.currentTimeMillis();
		final boolean mysql = Datamanager.databasetype.equals(Found.MYSQL);
		ETable.create(new BukkitRunnable() {
			@Override
			public void run() {
				final long after = System.currentTimeMillis();
				final double diff = (after - beginning) / 1000L;
				MessageUT.cmsg("Database successfuly load in " + diff + "s");
				MessageUT.cmsg("Total Data:");
				for (final ETable e : ETable.loaded) {
					MessageUT.cmsg(String.valueOf(e.getName()) + ": " + e.getLoadedData().size() + " data");
				}
			}
		}, mysql, Datamanager.table_item_sale, Datamanager.table_item_expired, Datamanager.table_item_sold,
				Datamanager.table_player_config);
	}

	public static void delete(final EObject data) {
		data.table.getLoadedData().remove(data);
		data.table.getLoadedValue().remove(data.getIdentifier());
		data.table.removeOwned(data.getOwner(), data);
		if (!Datamanager.databasetype.equals(Found.YML)) {
			final HashMap<String, Object> condition = new HashMap<>();
			condition.put(data.table.getPrimary(), data.getIdentifier());
			new BukkitRunnable() {
				@Override
				public void run() {
					SqlUT.executeUpdate(SqlUT.delete(data.table.getName(), condition));
				}
			}.runTask(Core.getThis());
		} else {
			final FileConfiguration fc = Core.databaseconfig.getConfig();
			final String name = data.getIdentifier();
			final String p = String.valueOf(data.table.getName()) + "." + name;
			fc.set(p, (Object) null);
			Core.databaseconfig.save();
		}
	}

	public static synchronized void save(final EObject data) {
		executeQueue(addQueue(data));
	}

	public static List<String> addQueue(final EObject data) {
		return addQueue(null, data);
	}

	public static List<String> addQueue(List<String> queue, final EObject data) {
		if (queue == null || queue.isEmpty()) {
			queue = new ArrayList<>();
		}
		final HashMap<String, Object> columns = data.getValues();
		if (!Datamanager.databasetype.equals(Found.YML)) {
			queue.add(SqlUT.insert(data.getTable().getName(), columns));
		} else {
			final FileConfiguration fc = Core.databaseconfig.getConfig();
			final String name = data.getIdentifier();
			final String p = String.valueOf(data.getTable().getName()) + "." + name + ".";
			for (final String key : data.getTable().getColumns().keySet()) {
				if (name.equals(columns.get(key))) {
					continue;
				}
				fc.set(String.valueOf(p) + key, columns.get(key));
			}
		}
		return queue;
	}

	public static synchronized void executeQueue(final List<String> queue) {
		if (!Datamanager.databasetype.equals(Found.YML)) {
			new BukkitRunnable() {
				@Override
				public void run() {
					SqlUT.executeBatch(queue);
				}
			}.runTask(Core.getThis());
		} else {
			Core.databaseconfig.save();
		}
	}

	public enum Found {
		SQLITE("SQLITE", 0, "SQLite"), MYSQL("MYSQL", 1, "MySQL"), YML("YML", 2, "yml");

		private String alias;

		private Found(final String s, final int n, final String alias) {
			this.alias = alias;
		}

		@Override
		public String toString() {
			return alias;
		}

		public static Found getType(final String type) {
			Found[] values;
			for (int length = (values = values()).length, i = 0; i < length; ++i) {
				final Found dt = values[i];
				if (dt.toString().equalsIgnoreCase(type)) {
					return dt;
				}
			}
			return Found.SQLITE;
		}
	}
}
