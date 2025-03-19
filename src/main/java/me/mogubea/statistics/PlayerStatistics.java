package me.mogubea.statistics;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class PlayerStatistics {

	final private SimpleStatHolder<Integer> stats = new SimpleStatHolder<>();
	final private SimpleStatHolder<Integer> dailyStats = new SimpleStatHolder<>();

	public int getStat(@NotNull SimpleStatType type, @NotNull String name) {
		return stats.getStat(type, name);
	}

	public int getDailyStat(@NotNull SimpleStatType type, @NotNull String name) {
		return dailyStats.getStat(type, name);
	}

	public void setStat(@NotNull SimpleStatType type, @NotNull String name, int value) {
		setStat(type, name, value, true);
	}

	public void setStat(@NotNull SimpleStatType type, @NotNull String name, int value, boolean dirty) {
		stats.setStat(type, name, value, dirty);
	}

	public void setDailyStat(@NotNull SimpleStatType type, @NotNull String name, int value) {
		setDailyStat(type, name, value, true);
	}

	public void setDailyStat(@NotNull SimpleStatType type, @NotNull String name, int value, boolean dirty) {
		dailyStats.setStat(type, name, value, dirty);
	}

	public void addToStat(@NotNull SimpleStatType type, @NotNull String name, int add) {
		stats.addToStat(type, name, add);
		dailyStats.addToStat(type, name, add);
	}

	public void incrementStat(@NotNull SimpleStatType type, @NotNull String name) {
		addToStat(type, name, 1);
	}

	public @NotNull HashMap<SimpleStatType, HashMap<String, DirtyVal<Integer>>> getMap() {
		return stats.getMap();
	}

	public @NotNull HashMap<SimpleStatType, HashMap<String, DirtyVal<Integer>>> getDailyMap() {
		return dailyStats.getMap();
	}

	public void clearDailyStats() {
		this.dailyStats.clear();
	}
	
}
