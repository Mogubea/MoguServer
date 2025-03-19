package me.mogubea.attributes;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AttributeValue {

    private final Attribute attribute;
    private double flatValue;
    private double percentageMultiplier;

    private double totalValue;

    private final List<AttributeModifier> modifiers = new ArrayList<>();
    private boolean dirty;

    protected AttributeValue(@NotNull Attribute attribute) {
        this.attribute = attribute;
        resetValues();
    }

    protected void recalculateValues() {
        resetValues();

        if (dirty) {
            List<AttributeModifier> modifierList = new ArrayList<>(modifiers);
            for (int x = -1; ++x < modifierList.size();) {
                AttributeModifier modifier = modifierList.get(x);
                if (modifier.isMultiplier()) {
                    percentageMultiplier += modifier.getValue();
                } else {
                    flatValue += modifier.getValue();
                }
            }
        }

        totalValue = flatValue * percentageMultiplier;

        // Check min max
        if (totalValue < attribute.getMinValue())
            totalValue = attribute.getMinValue();
        else if (totalValue > attribute.getMaxValue())
            totalValue = attribute.getMaxValue();

        dirty = false;
    }

    protected void addModifier(@NotNull AttributeModifier modifier) {
        modifiers.remove(modifier);
        modifiers.add(modifier);
        dirty = true;
    }

    protected void removeModifier(@NotNull AttributeModifier modifier) {
        modifiers.remove(modifier);
        dirty = true;
    }

    protected void removeItemModifiers(@NotNull ItemStack itemStack) {
        List<AttributeModifier> modifierList = new ArrayList<>(modifiers);
        for (int x = -1; ++x < modifierList.size();) {
            if (!(modifiers.get(x) instanceof AttributeModifierItem modifier)) continue;
            if (!modifier.getItemReference().equals(itemStack)) continue;

            removeModifier(modifier);
        }
    }

    protected void removeReforgeModifiers(@NotNull ItemStack itemStack) {
        List<AttributeModifier> modifierList = new ArrayList<>(modifiers);
        for (int x = -1; ++x < modifierList.size();) {
            if (!(modifiers.get(x) instanceof AttributeModifierReforge modifier)) continue;
            if (!modifier.getItemReference().equals(itemStack)) continue;

            removeModifier(modifier);
        }
    }

    protected double getTotal() {
        return totalValue;
    }

    protected double getMultiplier() {
        return percentageMultiplier;
    }

    protected double getFlatValue() {
        return flatValue;
    }

    protected @NotNull Attribute getAttribute() {
        return attribute;
    }

    private void resetValues() {
        this.flatValue = attribute.getMinValue();
        this.percentageMultiplier = 1;
    }

    protected void clear() {
        resetValues();
        this.modifiers.clear();
    }

}
