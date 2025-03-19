package me.mogubea.attributes;

import me.mogubea.main.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Attribute {

    private final String identifier;
    private final Component component;
    private final NamespacedKey key;
    private final UUID uuid;

    private double minValue = 0.0;
    private double maxValue = 2048.0;
    private org.bukkit.attribute.Attribute vanillaCounterpart;

    protected Attribute(@NotNull String identifier, @NotNull Component displayName) {
        this.identifier = identifier;
        this.component = displayName;
        this.key = new NamespacedKey(Main.getInstance(), "ATTR_" + identifier);
        this.uuid = UUID.randomUUID();
    }

    protected @NotNull Attribute setMaxValue(double maxValue) {
        if (maxValue < minValue) throw new UnsupportedOperationException("Maximum attribute value cannot be smaller than the minimum value.");
        this.maxValue = maxValue;
        return this;
    }

    protected @NotNull Attribute setMinValue(double minValue) {
        if (minValue > maxValue) throw new UnsupportedOperationException("Minimum attribute value cannot be larger than the maximum value.");
        this.minValue = minValue;
        return this;
    }

    protected @NotNull Attribute setBukkitAttribute(org.bukkit.attribute.Attribute attribute) {
        this.vanillaCounterpart = attribute;
        return this;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public @NotNull String getIdentifier() {
        return identifier;
    }

    public @NotNull Component getComponent() {
        return component;
    }

    public @NotNull NamespacedKey getNamespacedKey() {
        return key;
    }

    public @Nullable org.bukkit.attribute.Attribute getBukkitAttribute() {
        return vanillaCounterpart;
    }

    /**
     * This {@link UUID} is regenerated every restart. There is no reason to save a persistent {@link UUID} since we are using Transient Modifiers.
     * @return The {@link UUID} generated for this {@link Attribute} this cycle.
     */
    public @NotNull UUID getUniqueId() {
        return uuid;
    }

}
