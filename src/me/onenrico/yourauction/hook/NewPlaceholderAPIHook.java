package me.onenrico.yourauction.hook;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.onenrico.yourauction.main.YourAuctionAPI;
import me.onenrico.yourauction.object.ESoldItem;

public class NewPlaceholderAPIHook extends PlaceholderExpansion{

    /**
     * This method should always return true unless we
     * have a dependency we need to make sure is on the server
     * for our placeholders to work!
     *
     * @return always true since we do not have any dependencies.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * 
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
        return "Onenrico";
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest 
     * method to obtain a value if a placeholder starts with our 
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier(){
        return "ya";
    }

    /**
     * This is the version of this expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
        return "1.0.0";
    }
  
    /**
     * This is the method called when a placeholder with our identifier 
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A {@link org.bukkit.OfflinePlayer OfflinePlayer}.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return Possibly-null String of the requested identifier.
     */
    @SuppressWarnings("deprecation")
	@Override
    public String onRequest(OfflinePlayer player, String identifier){
  
		String prefix = identifier;
		OfflinePlayer ofc = player;
		if (identifier.contains(":")) {
			prefix = identifier.split(":")[0];
			ofc = Bukkit.getOfflinePlayer(identifier.split(":")[1]);
		}
		switch (prefix) {
		case "total_sale_items":
			return "" + YourAuctionAPI.getSaleItems().size();
		case "sale_items":
			return "" + YourAuctionAPI.getSaleItems(ofc.getUniqueId()).size();
		case "sold_items":
			return "" + YourAuctionAPI.getSoldItems(ofc.getUniqueId()).size();
		case "expired_items":
			return "" + YourAuctionAPI.getExpiredItems(ofc.getUniqueId()).size();
		case "untaken_sold_items":
			int result = 0;
			for (ESoldItem esi : YourAuctionAPI.getSoldItems(ofc.getUniqueId())) {
				if (!esi.isTaken()) {
					result++;
				}
			}
			return "" + result;
		case "untaken_money":
			double money = 0;
			for (ESoldItem esi : YourAuctionAPI.getSoldItems(ofc.getUniqueId())) {
				if (!esi.isTaken()) {
					money += esi.getTotalmoney();
				}
			}
			return "" + money;
		case "taken_money":
			double money2 = 0;
			for (ESoldItem esi : YourAuctionAPI.getSoldItems(ofc.getUniqueId())) {
				if (esi.isTaken()) {
					money2 += esi.getTotalmoney();
				}
			}
			return "" + money2;
		case "total_money":
			double money3 = 0;
			for (ESoldItem esi : YourAuctionAPI.getSoldItems(ofc.getUniqueId())) {
				money3 += esi.getTotalmoney();
			}
			return "" + money3;
		}
		return "Unknown Placeholder";
	}

	public static double round(double value, final int places) {
		if (places < 0) {
			throw new IllegalArgumentException();
		}
		final long factor = (long) Math.pow(10.0, places);
		value *= factor;
		final long tmp = Math.round(value);
		return tmp / factor;
	}
}
