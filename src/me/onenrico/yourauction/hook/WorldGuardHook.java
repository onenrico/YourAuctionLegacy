//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.hook;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;

import me.onenrico.yourauction.utils.MessageUT;
import me.onenrico.yourauction.utils.PermissionUT;

@SuppressWarnings({ "rawtypes" })
public class WorldGuardHook {
	public static Object yourauction_open = null;
	private static WorldGuardPlugin worldGuardPlugin = null;
	public static boolean success = true;


	public static WorldGuardPlugin getWorldGuard() {
		final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		if (plugin == null) {
			return null;
		}
		return WorldGuardHook.worldGuardPlugin = (WorldGuardPlugin) plugin;
	}

	public static boolean setup() {
		WorldGuardHook.yourauction_open = new StateFlag("yourauction-open", true);
		if (WorldGuardHook.worldGuardPlugin == null) {
			return WorldGuardHook.success = false;
		}
		if (WorldGuardHook.worldGuardPlugin.getDescription().getVersion().startsWith("6.2")) {
			final FlagRegistry registry = WorldGuardHook.worldGuardPlugin.getFlagRegistry();
			try {
				registry.register((Flag) WorldGuardHook.yourauction_open);
			} catch (Exception e) {
				MessageUT.cmsg("WorldGuard Flag Registered Already...");
				WorldGuardHook.yourauction_open = registry.get("yourauction-open");
				if (WorldGuardHook.yourauction_open == null) {
					WorldGuardHook.success = false;
					return false;
				}else {
					WorldGuardHook.success = true;
					return true;
				}
			}
		}
		if (WorldGuardHook.worldGuardPlugin.getDescription().getVersion().startsWith("7")) {
			final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
			try {
				registry.register((Flag) WorldGuardHook.yourauction_open);
			} catch (Exception e) {
				MessageUT.cmsg("WorldGuard Flag Registered Already...");
				WorldGuardHook.yourauction_open = registry.get("yourauction-open");
				if (WorldGuardHook.yourauction_open == null) {
					WorldGuardHook.success = false;
					return false;
				}else {
					WorldGuardHook.success = true;
					return true;
				}
			}
		}
		return true;
	}

	public static boolean canOpen(final Player p) {
		if (PermissionUT.has(p, "ya.open.bypass")) {
			return true;
		}
		if (WorldGuardHook.worldGuardPlugin == null || !WorldGuardHook.success) {
			return true;
		}
		final Location loc = p.getLocation();
		try {
			RegionManager manager = null;
			ApplicableRegionSet set = null;
			if (WorldGuardHook.worldGuardPlugin.getDescription().getVersion().startsWith("7")) {
				manager = WorldGuard.getInstance().getPlatform().getRegionContainer()
						.get(WorldGuard.getInstance().getPlatform().getMatcher().getWorldByName(loc.getWorld().getName()));
				final com.sk89q.worldedit.math.BlockVector3 vv = com.sk89q.worldedit.math.Vector3.toBlockPoint(loc.getX(), loc.getY(), loc.getZ());
				Method m = manager.getClass().getMethod("getApplicableRegions", com.sk89q.worldedit.math.BlockVector3.class);
				set = (ApplicableRegionSet)m.invoke(manager, vv);
			} else {
				manager = WorldGuardHook.worldGuardPlugin.getRegionManager(loc.getWorld());
				final com.sk89q.worldedit.Vector v = new com.sk89q.worldedit.Vector().setX(loc.getX()).setY(loc.getY()).setZ(loc.getZ());
				set = manager.getApplicableRegions(v);
			}

			if (set.queryState((RegionAssociable) null,
					new StateFlag[] { (StateFlag) WorldGuardHook.yourauction_open }) == StateFlag.State.DENY) {
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return true;
		}
		return true;
	}
}
