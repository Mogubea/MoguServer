package me.mogubea.items;

import me.mogubea.entities.CustomEntityType;
import me.mogubea.entities.MoguEntityCrop;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public class MoguItemSeeds extends MoguItemBlock {

    private final CustomEntityType<MoguEntityCrop> cropType;

    protected MoguItemSeeds(@NotNull MoguItemManager manager, @NotNull String identifier, @NotNull String displayName, @NotNull ItemRarity rarity, @NotNull CustomEntityType<MoguEntityCrop> crop) {
        super(manager, identifier, displayName, Material.WHEAT_SEEDS, rarity);
        this.cropType = crop;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
            event.getItemInHand().setAmount(event.getItemInHand().getAmount() - 1);
        cropType.spawn(event.getBlock().getLocation());
    }
}
