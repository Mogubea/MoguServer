package me.mogubea.attributes;

import me.mogubea.items.reforges.Reforge;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AttributeModifierReforge extends AttributeModifierItem {

    private final Reforge reforge;

    public AttributeModifierReforge(@NotNull ItemStack itemStack, @NotNull Reforge reforge, double value, boolean isMultiplier) {
        super(itemStack, value, isMultiplier);
        this.reforge = reforge;
    }

    public @NotNull Reforge getReforge() {
        return reforge;
    }

    @Override
    public @NotNull Component getDescription() {
        return reference.displayName().append(Component.text("'s " + getReforge().getName() + " Reforge"));
    }

}
