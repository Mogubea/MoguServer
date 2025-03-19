package me.mogubea.attributes;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Attributes {

    private static List<Attribute> ATTRIBUTE_LIST = new ArrayList<>();

    public static final Attribute MAXIMUM_HEALTH;
    public static final Attribute MOVEMENT_SPEED;

    public static final Attribute ATTACK_SPEED;

    public static final Attribute BASE_DAMAGE;
    public static final Attribute MELEE_DAMAGE;
    public static final Attribute RANGED_DAMAGE;
    public static final Attribute CRITICAL_DAMAGE;
    public static final Attribute LIFE_STEAL;

    public static final Attribute DODGE_CHANCE;

    static {
        // Base game stats
        MAXIMUM_HEALTH = register(new Attribute("MAXIMUM_HEALTH", Component.text("Health"))).setMinValue(1).setMaxValue(10000).setBukkitAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        MOVEMENT_SPEED = register(new Attribute("MOVEMENT_SPEED", Component.text("Speed"))).setMinValue(25).setMaxValue(500).setBukkitAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED);
        ATTACK_SPEED = register(new Attribute("ATTACK_SPEED", Component.text("Attack Speed"))).setMaxValue(5).setBukkitAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_SPEED);

        // Custom stats
        BASE_DAMAGE = register(new Attribute("BASE_DAMAGE", Component.text("Base Damage"))).setMinValue(1);
        MELEE_DAMAGE = register(new Attribute("MELEE_DAMAGE", Component.text("Melee Damage"))).setMinValue(0);
        RANGED_DAMAGE = register(new Attribute("RANGED_DAMAGE", Component.text("Ranged Damage"))).setMinValue(0);
        CRITICAL_DAMAGE = register(new Attribute("CRITICAL_DAMAGE", Component.text("Critical Damage"))).setMinValue(0);
        LIFE_STEAL = register(new Attribute("LIFE_STEAL", Component.text("Life Steal"))).setMinValue(0);

        DODGE_CHANCE = register(new Attribute("DODGE_CHANCE", Component.text("Dodge Chance"))).setMinValue(0).setMaxValue(100);

        // Make unmodifiable
        ATTRIBUTE_LIST = Collections.unmodifiableList(ATTRIBUTE_LIST);
    }

    public static @NotNull List<Attribute> getAttributeList() {
        return ATTRIBUTE_LIST;
    }

    private static Attribute register(@NotNull Attribute attribute) {
        ATTRIBUTE_LIST.add(attribute);
        return attribute;
    }

}
