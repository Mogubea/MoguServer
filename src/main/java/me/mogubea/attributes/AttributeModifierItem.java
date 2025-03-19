package me.mogubea.attributes;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AttributeModifierItem extends AttributeModifier {

    protected final ItemStack reference;

    public AttributeModifierItem(@NotNull ItemStack itemStack, double value, boolean isMultiplier) {
        super(value, isMultiplier);
        this.reference = itemStack;
    }

    public @NotNull ItemStack getItemReference() {
        return reference;
    }

    @Override
    public @NotNull Component getDescription() {
        return reference.displayName();
    }

}
