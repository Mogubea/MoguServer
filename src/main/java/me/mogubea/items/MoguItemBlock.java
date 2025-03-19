package me.mogubea.items;

import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public abstract class MoguItemBlock extends MoguItem {

    protected MoguItemBlock(@NotNull MoguItemManager manager, @NotNull String identifier, @NotNull String displayName, @NotNull Material material, @NotNull ItemRarity rarity) {
        super(manager, identifier, displayName, material, rarity);
    }

    public abstract void onBlockPlace(BlockPlaceEvent event);

}
