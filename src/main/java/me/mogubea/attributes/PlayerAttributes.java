package me.mogubea.attributes;

import me.mogubea.profile.PlayerProfile;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerAttributes {

    private final Map<Attribute, AttributeValue> valuesByAttribute = new HashMap<>();
    private final List<AttributeValue> attributeValues = new ArrayList<>();
    private final PlayerProfile profile;

    public PlayerAttributes(@NotNull PlayerProfile profile) {
        this.profile = profile;

        for (int x = -1; ++x < Attributes.getAttributeList().size();) {
            Attribute attribute = Attributes.getAttributeList().get(x);
            AttributeValue value = new AttributeValue(attribute);
            valuesByAttribute.put(attribute, value);
            attributeValues.add(value);
        }
    }

    public void addModifier(@NotNull Attribute attribute, @NotNull AttributeModifier modifier) {
        this.valuesByAttribute.get(attribute).addModifier(modifier);
    }

    public void addModifiers(@NotNull Map<Attribute, AttributeModifier> modifiers) {
        modifiers.forEach(this::addModifier);
    }

    public double getValue(@NotNull Attribute attribute) {
        return valuesByAttribute.get(attribute).getTotal();
    }

    /**
     * Apply attributes to the player that require the usage of {@link org.bukkit.attribute.Attribute}s.
     */
    protected void apply() {
        Player player = profile.getOfflinePlayer().getPlayer();
        if (player == null) return;

        for (int wayoy = -1; ++wayoy < attributeValues.size();) {
            AttributeValue value = attributeValues.get(wayoy);
            Attribute attribute = value.getAttribute(); // screw the map
            if (attribute.getBukkitAttribute() != null) {
                AttributeInstance bukkitInstance = player.getAttribute(attribute.getBukkitAttribute());
                if (bukkitInstance == null)
                    player.registerAttribute(attribute.getBukkitAttribute());
                assert bukkitInstance != null;

                // TODO: Move this.
                double total = value.getTotal();
                if (bukkitInstance.getAttribute() == org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED)
                    total /= 1000;

                org.bukkit.attribute.AttributeModifier bukkitModifier = new org.bukkit.attribute.AttributeModifier(
                        attribute.getUniqueId(), attribute.getBukkitAttribute().translationKey(), total, org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER);

                bukkitInstance.removeModifier(bukkitModifier);
                bukkitInstance.addTransientModifier(bukkitModifier);
            }
        }
    }

    /**
     * For use whenever a modifier has been added or removed.
     */
    public void calculate() {
        attributeValues.forEach(AttributeValue::recalculateValues);
        apply();
    }

    /**
     * For use when the player logs out
     */
    public void clear() {
        for (int x = -1; ++ x < attributeValues.size();)
            attributeValues.get(x).clear();
        apply();
    }

}
