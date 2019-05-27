//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.onenrico.yourauction.utils.EMaterial;

public class ConfigManager {
	public static List<String> aliases = null;
	public static double tax;
	public static int defaultmaxsell;
	public static double saleHour;
	public static double minsaleHour;
	public static double destroyHour;
	public static String salecost;
	public static boolean allowcreative;
	public static boolean allowdamaged;

	public static boolean enabledsellall;
	public static boolean allowcreative2;
	public static boolean allowdamaged2;
	public static boolean showback;
	public static double tax2;

	public static double maxprice;
	public static boolean instantautocollect;
	public static boolean autocollect;
	public static Set<Material> blacklist = new HashSet<>();
	public static List<ItemStack> tools_animation = new ArrayList<>();
	public static List<ItemStack> blocks_animation = new ArrayList<>();
	public static HashMap<EMaterial, Double> price_data = new HashMap<>();
	public static List<EMaterial> ordered_data = new ArrayList<>();
	public static boolean animation_enabled;
	public static double maxHour;
	public static String day = "d";
	public static String hour = "h";
	public static String minute = "m";
	public static String second = "s";
	public static boolean christmas;
	public static boolean compact;
}
