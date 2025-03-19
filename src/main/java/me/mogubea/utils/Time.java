package me.mogubea.utils;

import org.bukkit.World;

public class Time {
	
	public static int getTime(World w) {
		return (int) ((w.getTime()+1) / 83) * 83;
	}
	
	public static int getHour(int tick) {
		return (tick+6000) / 1000;
	}
	
	public static int getTwelfth(int tick) {
		return (((tick+6000) % 1000) / 83) * 5;
	}
	
	public static String getTimeString(int tick, boolean ampm) {
		int hour = getHour(tick);
		final int minute = getTwelfth(tick);
		final boolean afternoon = hour > 11 && hour < 24;
		
		if (ampm && hour > 24)
			hour -= 24;
		else if (ampm && hour > 12)
			hour -= 12;
		
		return (hour<10 ? "0" : "") + hour + ":" + (minute<10 ? "0" : "") + minute + (ampm ? (afternoon ? " pm" : " am") : "");
	}
	
	public static int getDay(long longtick) {
		return (int) (((longtick+6000) / 24000) + 1);
	}

	public static String stringFromNow(long timeInMillis) {
		long cur = System.currentTimeMillis();
		long secs = (timeInMillis > cur ? timeInMillis - cur : cur - timeInMillis) / 1000;
		long mins = secs / 60;
		secs -= mins*60;
		long hours = mins / 60;
		mins -= hours*60;
		long days = hours / 24;
		hours -= days*24;
		long weeks = days / 7;
		days -= weeks*7;

		if (weeks > 0)
			return weeks + (weeks > 1 ? " Weeks" : " Week") + (days > 0 ? " and " + days + (days > 1 ? " Days" : " Day") : "");
		if (days > 0)
			return days + (days > 1 ? " Days" : " Day") + (hours > 0 ? " and " + hours + (hours > 1 ? " Hours" : " Hour") : "");
		if (hours > 0)
			return hours + (hours > 1 ? " Hours" : " Hour") + (mins > 0 ? " and " + mins + (mins > 1 ? " Minutes" : " Minute") : "");
		if (mins > 0)
			return mins + (mins > 1 ? " Minutes" : " Minute") + (secs > 0 ? " and " + secs + (secs > 1 ? " Seconds" : " Second") : "");

		return secs + (secs > 1 ? " Seconds" : " Second");
	}

	public static String smallStringFromNow(long timeInMillis) {
		long cur = System.currentTimeMillis();
		long secs = (timeInMillis > cur ? timeInMillis - cur : cur - timeInMillis) / 1000;
		long mins = secs / 60;
		secs -= mins*60;
		long hours = mins / 60;
		mins -= hours*60;
		long days = hours / 24;
		hours -= days*24;

		if (days > 0)
			return days + "d" + hours + "h" + mins + "m";
		if (hours > 0)
			return hours + "h" + mins + "m" + secs + "s";
		if (mins > 0)
			return mins + "m" + secs + "s";

		return secs + "s";
	}

	public static String millisToString(long timeInMillis, boolean small) {
		long cur = System.currentTimeMillis();
		return small ? smallStringFromNow(timeInMillis + cur) : stringFromNow(timeInMillis + cur);
	}
	
}
