package me.mogubea.claims.flags;

import me.mogubea.utils.lore.Lore;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public final class Flags {
	
	private static final LinkedHashMap<String, Flag<?>> flagsByName = new LinkedHashMap<>();

	public static final FlagMember BUILD_ACCESS = register(new FlagMember("build-access", "Build Access", MemberLevel.MEMBER))
			.setDescription(Lore.fastBuild(false, 40, "Players with this membership level will be able to place, break and use blocks."));

	public static final FlagMember CROP_HARVESTING = register(new FlagMember("crop-access", "Crop Harvesting", MemberLevel.MEMBER))
			.setDescription(Lore.fastBuild(false, 40, "Players with this membership level will be able to harvest crops."));

	private static <T extends Flag<?>> T register(final T flag) {
		flagsByName.put(flag.getIdentifier(), flag);
		return flag;
	}
	
	public static ArrayList<Flag<?>> getRegisteredFlags() {
		return new ArrayList<>(flagsByName.values());
	}
	
	public static Flag<?> getFlag(String name) {
		return flagsByName.get(name);
	}
	
}
