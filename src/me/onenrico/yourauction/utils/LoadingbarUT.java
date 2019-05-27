//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.utils;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import me.onenrico.yourauction.locale.Locales;
import me.onenrico.yourauction.manager.PlaceholderManager;

public class LoadingbarUT {
	public static String getBar(final int totalbar, final double value, final double maxvalue, String symbol,
			final Boolean reverse, String filledcolor, String unfilledcolor) {
		final PlaceholderManager pm = new PlaceholderManager(Locales.pm);
		symbol = pm.process(symbol);
		final int persentase = (int) MathUT.getPersentase(value, maxvalue);
		final int fill = (int) MathUT.getRealvalue(totalbar, persentase);
		final String temp = filledcolor;
		if (reverse) {
			filledcolor = pm.process(unfilledcolor);
			unfilledcolor = pm.process(temp);
		}
		final String filled = StringUtils.repeat(symbol, fill);
		final String empty = StringUtils.repeat(symbol, totalbar - fill);
		final String result = String.valueOf(filledcolor) + filled + unfilledcolor + empty;
		return result;
	}

	public static String getBarFromString(final String bar) {
		final HashMap<String, String> value = ParserUT.extract("bar", bar).get(0);
		final int totalbar = MathUT.strInt(value.getOrDefault("bars", "10"));
		final String symbol = value.getOrDefault("symbol", "\ufffd?");
		final String filledcolor = value.getOrDefault("fill", "&f");
		final String unfilledcolor = value.getOrDefault("empty", "&8");
		final double v = MathUT.safe(value.getOrDefault("value", "0"));
		final double maxvalue = MathUT.safe(value.getOrDefault("maxvalue", "0"));
		return getBar(totalbar, v, maxvalue, symbol, false, filledcolor, unfilledcolor);
	}

	public static String getBar(final int totalbar, final int value, final int maxvalue, final String symbol) {
		return getBar(totalbar, value, maxvalue, symbol, false);
	}

	public static String getBar(final int totalbar, final int value, final int maxvalue, final String symbol,
			final Boolean reverse) {
		return getBar(totalbar, value, maxvalue, symbol, reverse, "&f", "&8");
	}
}
