package me.onenrico.yourauction.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import me.onenrico.yourauction.manager.ConfigManager;

public class CommandListener implements Listener {
	@EventHandler
	public void onCmd(PlayerCommandPreprocessEvent e) {
		String cmd = e.getMessage();
		String left = cmd.split(" ")[0];
		if (ConfigManager.aliases.contains(left.toLowerCase().replace("/", ""))) {
			e.setMessage(cmd.replace(left, "/yourauction"));
		}
	}
}
