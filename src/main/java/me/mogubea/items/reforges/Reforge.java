package me.mogubea.items.reforges;

import me.mogubea.attributes.Attribute;
import me.mogubea.attributes.AttributeModifier;
import me.mogubea.attributes.AttributeModifierReforge;
import me.mogubea.main.Main;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reforge {

    public static final NamespacedKey KEY_REFORGE = new NamespacedKey(Main.getInstance(), "REFORGE");

    private final String identifier;
    private final String displayName;
    private final ReforgeTarget[] targets;

    private final List<ReforgeModifier> modifiers = new ArrayList<>();

    private double weight;
    private boolean bad = false;

    protected Reforge(@NotNull String identifier, @NotNull String displayName, @NotNull ReforgeTarget... targets) {
        this.identifier = identifier.toUpperCase();
        this.displayName = displayName;
        this.targets = targets;
        this.weight = 1;
    }

    public @NotNull String getName() {
        return displayName;
    }

    protected @NotNull Reforge setWeight(double weight) {
        this.weight = weight;
        return this;
    }

    protected double getWeight() {
        return weight;
    }

    protected @NotNull Reforge setBad() {
        this.bad = true;
        return this;
    }

    protected boolean isBad() {
        return bad;
    }

    protected @NotNull Reforge addModifier(@NotNull ReforgeModifier modifier) {
        modifiers.add(modifier);
        return this;
    }

    protected @NotNull List<ReforgeModifier> getModifiers() {
        return modifiers;
    }

    protected @NotNull Map<Attribute, List<AttributeModifier>> createAttributeModifiers(@NotNull ItemStack itemStack) {
        Map<Attribute, List<AttributeModifier>> attributeModifiers = new HashMap<>();
        for (int x = -1; ++x < modifiers.size();) {
            ReforgeModifier mod = modifiers.get(x);
            double value = mod.isMultiplier() && mod.appliesToItem() ? 0 /* TODO: item multiplier */ : mod.getValue();

            if (!attributeModifiers.containsKey(mod.getAttribute()))
                attributeModifiers.put(mod.getAttribute(), new ArrayList<>());

            attributeModifiers.get(mod.getAttribute()).add(new AttributeModifierReforge(itemStack, this, value, mod.isMultiplier()));
        }

        return attributeModifiers;
    }

    protected @NotNull ReforgeTarget[] getTargets() {
        return targets;
    }

    protected @NotNull String getIdentifier() {
        return identifier;
    }

}
