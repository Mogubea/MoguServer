package me.mogubea.items;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.jetbrains.annotations.NotNull;

public class MoguItemEdible extends MoguItem {

    private int foodValue;
    private float saturationValue;

    protected MoguItemEdible(@NotNull MoguItemManager manager, @NotNull String identifier, @NotNull String displayName, @NotNull Material material, @NotNull ItemRarity rarity) {
        super(manager, identifier, displayName, material, rarity);
        if (!material.isEdible()) throw new RuntimeException("Instances of MoguItemEdible must be edible.");
    }

    /**
     * Set the amount of food that gets restored upon eating this item.
     * @param foodValue The value
     * @return The {@link MoguItemEdible} instance.
     */
    protected MoguItemEdible setFoodValue(int foodValue) {
        this.foodValue = foodValue;
        return this;
    }

    /**
     * Set the amount of saturation that gets restored upon eating this item.
     * @param saturationValue The value
     * @return The {@link MoguItemEdible} instance.
     */
    protected MoguItemEdible setSaturationValue(float saturationValue) {
        this.saturationValue = saturationValue;
        return this;
    }

    public int getFoodValue() {
        return foodValue;
    }

    public float getSaturationValue() {
        return saturationValue;
    }

    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (foodValue >= 0)
            event.getPlayer().setFoodLevel(Math.min(20, event.getPlayer().getFoodLevel() + foodValue));
        if (saturationValue >= 0)
            event.getPlayer().setSaturation(Math.min(4F, event.getPlayer().getSaturation() + saturationValue));
    }

}
