package me.mogubea.items;

import me.mogubea.entities.CustomEntityType;
import me.mogubea.main.Main;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class MoguItems {

    private static final MoguItemManager manager = Main.getInstance().getItemManager();

    public static final MoguItem TEST_FISH;
    public static final MoguItem TEST_FISH_2;
    public static final MoguItem BARLEY_SEEDS;
    public static final MoguItem WITHERED_BONE;
    public static final MoguItem WITHERED_BONE_MEAL;
    public static final MoguItem SILLY_XP_JAR;

    static {
        TEST_FISH = registerItem(new MoguItemEdible(manager, "TEST_FISH", "Test Fish", Material.COD, ItemRarity.EXCEPTIONAL));
        TEST_FISH_2 = registerItem(new MoguItemEdible(manager, "TEST_FISH_2", "Test Fish 2", Material.COD, ItemRarity.UNCOMMON));
        BARLEY_SEEDS = registerItem(new MoguItemSeeds(manager, "BARLEY_SEEDS", "Barley Seeds", ItemRarity.UNCOMMON, CustomEntityType.CROP_BARLEY));
        WITHERED_BONE = registerItem(new MoguItem(manager, "WITHERED_BONE", "Withered Bone", Material.BONE, ItemRarity.COMMON));
        WITHERED_BONE_MEAL = registerItem(new MoguItemWitheredBonemeal(manager, "WITHERED_BONE_MEAL", "Withered Bone Meal", Material.BONE_MEAL, ItemRarity.COMMON));
        SILLY_XP_JAR = registerItem(new MoguItemExperienceJar(manager, "SILLY_XP_JAR", "Silly Experience Jar", Material.PLAYER_HEAD, ItemRarity.REMARKABLE));
    }

    private static @NotNull MoguItem registerItem(@NotNull MoguItem item) {
        return manager.register(item);
    }

}
