//
// Decompiled by Procyon v0.5.30
//

package me.onenrico.yourauction.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import me.onenrico.yourauction.manager.ConfigManager;

public class TimeManager {
	private long created;

	public TimeManager() {
		created = System.currentTimeMillis();
	}

	public void hit(final String prefix) {
		MessageUT.cmsg(String.valueOf(prefix) + " " + (System.currentTimeMillis() - created) + "ms");
	}

	public void hit() {
		this.hit("Elapsed:");
	}

	public static String formatTime(long second) {
		if (second < 0L) {
			return "...";
		}
		int day = 0;
		int hour = 0;
		int minute = 0;
		while (second >= 86400L) {
			++day;
			second -= 86400L;
		}
		while (second >= 3600L) {
			++hour;
			second -= 3600L;
		}
		while (second >= 60L) {
			++minute;
			second -= 60L;
		}
		final StringBuilder build = new StringBuilder();
		if (day > 0) {
			build.append(String.valueOf(day) + ConfigManager.day);
			build.append(" ");
		}
		if (hour > 0) {
			build.append(String.valueOf(hour) + ConfigManager.hour);
			build.append(" ");
		}
		if (minute > 0) {
			build.append(String.valueOf(minute) + ConfigManager.minute);
			build.append(" ");
		}
		if (second > 0L) {
			build.append(String.valueOf(second) + ConfigManager.second);
		}
		return build.toString();
	}

	public static Date fromString(String date) {
		final DateFormat df = new SimpleDateFormat("dd/MM/yyyy H:mm:ss:SSS");
		Date startDate = null;
		try {
			if (date == null || date.equalsIgnoreCase("null")) {
				date = toString(new Date());
			}
			startDate = df.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return startDate;
	}

	public static Date fromLong(final long date) {
		final Date startDate = new Date(date);
		return startDate;
	}

	public static Date getNow() {
		return new Date();
	}

	public static String toString(final Date date) {
		final DateFormat df = new SimpleDateFormat("dd/MM/yyyy H:mm:ss:SSS");
		return df.format(date);
	}

	public static int getDay(final Date date1, final Date date2) {
		final long diff = getDifferent(date1, date2);
		final int days = (int) (diff / 86400L);
		return days;
	}

	public static long getSecond(final Date date1, final Date date2) {
		final long diff = getDifferent(date1, date2);
		return diff / 1000L;
	}

	public static long getSecond(final long date1, final long date2) {
		final long diff = date2 - date1;
		return diff / 1000L;
	}

	public static long getDifferent(final Date date1, final Date date2) {
		final long diff = date2.getTime() - date1.getTime();
		return diff;
	}

	public static long getDifferent(final String date1, final String date2) {
		return getDifferent(fromString(date1), fromString(date2));
	}

	public static Date getFuture(final Date date, final int day) {
		final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy H:mm:ss:SSS");
		final Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(5, day);
		final String output = df.format(c.getTime());
		return fromString(output);
	}

	public static Date getFuture(final Date date, final long second) {
		date.setTime(date.getTime() + second * 1000L);
		return date;
	}
}
