package me.mogubea.attributes;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public abstract class AttributeModifier {

    private final double value;
    private final boolean isMultiplier;

    public AttributeModifier(double value, boolean isMultiplier) {
        this.value = value;
        this.isMultiplier = isMultiplier;
    }

    public double getValue() {
        return value;
    }

    public boolean isMultiplier() {
        return isMultiplier;
    }

    public abstract @NotNull Component getDescription();

}
