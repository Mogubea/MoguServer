package me.mogubea.items.reforges;

import me.mogubea.attributes.Attribute;
import org.jetbrains.annotations.NotNull;

public class ReforgeModifier {

    private final Attribute attribute;
    private final boolean multiplier;
    private final double amount;
    private final boolean applyToItem;

    protected ReforgeModifier(@NotNull Attribute attribute, double amount, boolean multiply, boolean applyToItem) {
        this.attribute = attribute;
        this.multiplier = multiply;
        this.amount = amount;
        this.applyToItem = applyToItem;
    }

    protected boolean isMultiplier() {
        return multiplier;
    }

    protected double getValue() {
        return amount;
    }

    protected boolean appliesToItem() {
        return applyToItem;
    }

    public @NotNull Attribute getAttribute() {
        return attribute;
    }

}
