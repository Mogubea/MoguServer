package me.mogubea.items.reforges;

import me.mogubea.attributes.Attributes;
import me.mogubea.main.Main;
import org.jetbrains.annotations.NotNull;

public class Reforges {

    private static final ReforgeManager manager = Main.getInstance().getItemManager().getReforgeManager();

    public static final Reforge DAMAGED;
    public static final Reforge BROKEN;
    public static final Reforge FRAGILE;

    static {
        // Durable Items
        FRAGILE = register(new Reforge("FRAGILE", "Fragile", ReforgeTarget.DURABLE)).setWeight(1.2).setBad()
                .addModifier(new ReforgeModifier(Attributes.ATTACK_SPEED, -0.1, true, true));
        DAMAGED = register(new Reforge("DAMAGED", "Damaged", ReforgeTarget.DURABLE)).setWeight(0.8).setBad()
                .addModifier(new ReforgeModifier(Attributes.ATTACK_SPEED, -0.2, true, true))
                .addModifier(new ReforgeModifier(Attributes.BASE_DAMAGE, -0.05, true, true));
        BROKEN = register(new Reforge("BROKEN", "Broken", ReforgeTarget.DURABLE)).setWeight(0.7).setBad()
                .addModifier(new ReforgeModifier(Attributes.ATTACK_SPEED, -0.3, true, true))
                .addModifier(new ReforgeModifier(Attributes.BASE_DAMAGE, -0.15, true, true))
                .addModifier(new ReforgeModifier(Attributes.CRITICAL_DAMAGE, -10, false, false));
    }

    private static @NotNull Reforge register(@NotNull Reforge reforge) {
        return manager.register(reforge);
    }

}
