package me.onenrico.yourauction.locale;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.text.StrLookup;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.onenrico.yourauction.main.Core;
import me.onenrico.yourauction.manager.PlaceholderManager;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Locales {
	private static HashMap<String, List<String>> messages = new HashMap<>();
	private static FileConfiguration config = null;
	private static Random rand = new Random();
	public static PlaceholderManager pm;
	public static String rawpluginPrefix;
	public static String jsonpluginPrefix;
	private static File file;

	public static void setup() {
		Locales.pm = new PlaceholderManager();
		final Set<String> msgkeys = Locales.config.getConfigurationSection("messages").getKeys(false);
		if (msgkeys != null) {
			for (final String key : msgkeys) {
				Locales.messages.put(key, Locales.config.getStringList("messages." + key));
			}
		}
		final Set<String> cpkeys = Locales.config.getConfigurationSection("custom-placeholder").getKeys(false);

		if (cpkeys != null) {
			for (final String key : cpkeys) {
				pm.add(key, Locales.config.getString("custom-placeholder." + key));
			}
		}

		ConfigurationSection cs = Locales.config.getConfigurationSection("random-placeholder");
		if (cs == null) {
			final List<String> values = new ArrayList<>();
			Locales.config.set("random-placeholder.yellow", new ArrayList(values));
			try {
				Locales.config.save(Locales.file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			cs = Locales.config.getConfigurationSection("random-placeholder");
		}
		final Set<String> rpkeys = cs.getKeys(false);
		if (rpkeys != null) {
			for (final String key : rpkeys) {
				pm.add(key, new StrLookup() {
					List<String> values = Locales.config.getStringList("random-placeholder." + key);

					@Override
					public String lookup(String arg0) {
						return values.get(rand.nextInt(values.size()));
					}
				});
			}
		}
	    rawpluginPrefix = Core.configplugin.getStr("pluginPrefix", "&cNot Configured");
	    jsonpluginPrefix = rawpluginPrefix;
	}

	public static List<String> getValue(final String msg) {
		if (Locales.messages.get(msg) == null) {
			final InputStream is = Core.getThis().getResource("lang_EN.yml");
			final File tfile = new File(Core.getThis().getDataFolder(), "lang.temp");
			try {
				Files.copy(is, tfile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			} catch (IOException e) {
				e.printStackTrace();
			}
			final FileConfiguration defaultc = YamlConfiguration.loadConfiguration(tfile);
			final List<String> mmsg = defaultc.getStringList("messages." + msg);
			Locales.config.set("messages." + msg, mmsg);
			Locales.messages.put(msg, mmsg);
			try {
				Locales.config.save(Locales.file);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			tfile.delete();
		}
		return pm.process(Locales.messages.get(msg));
	}

	public static void reload(final String locale) {
		try {
			Locales.file = new File(Core.getThis().getDataFolder(), "lang_" + locale + ".yml");
			if (!Locales.file.getParentFile().exists()) {
				Locales.file.getParentFile().mkdir();
			}
			if (!Locales.file.exists()) {
				Core.getThis().saveResource("lang_EN.yml", false);
			}
			Locales.config = YamlConfiguration.loadConfiguration(Locales.file);
			setup();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save() {
		try {
			Locales.config.save(Locales.file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
