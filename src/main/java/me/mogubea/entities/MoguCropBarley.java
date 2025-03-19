package me.mogubea.entities;

import me.mogubea.items.MoguItem;
import me.mogubea.items.MoguItems;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MoguCropBarley extends MoguEntityCrop {

    protected MoguCropBarley(Location location) {
        super(location);
//        setSmall(false);
    }

    @Override
    public byte getMaxAge() {
        return 5;
    }

    @Override
    protected short generateTicksForNextAge() {
        return (short) (2000 + random.nextInt(5000));
    }

    @Override
    protected @NotNull MoguItem getSeeds() {
        return MoguItems.BARLEY_SEEDS;
    }

    @Override
    protected @NotNull ItemStack getDrop() {
        return new ItemStack(Material.DIRT, 1 + random.nextInt(3));
    }

    @Override
    public short getBonemealEffectiveness() {
        return 1500;
    }

    @Override
    public int getEssenceValue() {
        return 10;
    }

    @Override
    protected @NotNull String getStatName() {
        return "BARLEY";
    }

    @Override
    protected int getCustomModelData() {
        return 5000;
    }

    @Override
    protected double getYOffset() {
        return -0.9D;
    }

}
