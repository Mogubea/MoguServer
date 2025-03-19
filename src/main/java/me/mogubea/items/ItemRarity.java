package me.mogubea.items;

import me.mogubea.utils.LatinSmall;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.HashMap;

public enum ItemRarity {

    TRASH(TextColor.color(0x585858)),
    COMMON(TextColor.color(0xFEFEFE)),
    UNCOMMON(TextColor.color(0x6EE763)),
    REMARKABLE(TextColor.color(0x6CAAE8)),
    OUTSTANDING(TextColor.color(0x3232C8)),
    EXCEPTIONAL(TextColor.color(0xD92AEC)),
    LEGENDARY(TextColor.color(0xFFB80C)),

    ADMIN(TextColor.color(0xff5555)),

    ;

    private final String name;
    private final String smallName;
    private final TextColor colour;

    ItemRarity(TextColor colour) {
        this.name = name().toLowerCase();
        this.smallName = LatinSmall.translate(name);
        this.colour = colour;
    }

    public @NotNull TextColor getColour() {
        return colour;
    }

    public @NotNull String getSmallName() {
        return smallName;
    }

    private final static HashMap<Material, ItemRarity> vanillaRarities = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = 1018055300807913156L;

        {
            for (Material m : Material.values()) {
                if (m.name().endsWith("SHULKER_BOX"))
                    put(m, ItemRarity.REMARKABLE);
                else if (m.name().startsWith("NETHERITE_"))
                    put(m, ItemRarity.UNCOMMON);
                else if (m.name().contains("COMMAND_BLOCK"))
                    put(m, ItemRarity.ADMIN);
            }

            put(Material.SKELETON_SKULL, ItemRarity.UNCOMMON);
            put(Material.CREEPER_HEAD, ItemRarity.UNCOMMON);
            put(Material.PLAYER_HEAD, ItemRarity.UNCOMMON);
            put(Material.ZOMBIE_HEAD, ItemRarity.UNCOMMON);
            put(Material.WITHER_SKELETON_SKULL, ItemRarity.UNCOMMON);
            put(Material.HEART_OF_THE_SEA, ItemRarity.UNCOMMON);
            put(Material.ANCIENT_DEBRIS, ItemRarity.UNCOMMON);
            put(Material.ECHO_SHARD, ItemRarity.UNCOMMON);
            put(Material.CONDUIT, ItemRarity.UNCOMMON);
            put(Material.DRAGON_HEAD, ItemRarity.UNCOMMON);
            put(Material.TRIDENT, ItemRarity.UNCOMMON);

            put(Material.NETHER_STAR, ItemRarity.REMARKABLE);
            put(Material.BEACON, ItemRarity.REMARKABLE);
            put(Material.DRAGON_EGG, ItemRarity.REMARKABLE);
            put(Material.SPAWNER, ItemRarity.REMARKABLE);
            put(Material.ENCHANTED_GOLDEN_APPLE, ItemRarity.REMARKABLE);
            put(Material.TOTEM_OF_UNDYING, ItemRarity.REMARKABLE);

            put(Material.ELYTRA, ItemRarity.OUTSTANDING);

            put(Material.LIGHT, ItemRarity.ADMIN);
            put(Material.END_PORTAL_FRAME, ItemRarity.ADMIN);
            put(Material.BEDROCK, ItemRarity.ADMIN);
            put(Material.DEBUG_STICK, ItemRarity.ADMIN);
            put(Material.BARRIER, ItemRarity.ADMIN);
            put(Material.STRUCTURE_BLOCK, ItemRarity.ADMIN);
            put(Material.STRUCTURE_VOID, ItemRarity.ADMIN);
            put(Material.JIGSAW, ItemRarity.ADMIN);
            put(Material.KNOWLEDGE_BOOK, ItemRarity.ADMIN);
        }
    };

    public static @NotNull ItemRarity getVanillaRarity(@NotNull ItemStack itemStack) {
        return getVanillaRarity(itemStack.getType());
    }

    public static @NotNull ItemRarity getVanillaRarity(@NotNull Material material) {
        return vanillaRarities.getOrDefault(material, ItemRarity.COMMON);
    }

}
