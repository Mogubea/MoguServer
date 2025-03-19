package me.mogubea.guilds;

import me.mogubea.entities.MoguEntityCrop;
import me.mogubea.events.HarvestCropEvent;
import me.mogubea.events.MoguCropInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class GuildFarming extends Guild {

    @Override
    protected void onCustomCropInteract(@NotNull GuildEventWrapper<MoguCropInteractEvent> wrapper) {
        if (!wrapper.getEvent().canHarvest()) return;
        MoguEntityCrop crop = wrapper.getEvent().getCrop();

        spawnEssenceOrb(crop.getTrueLocation(), crop.getEssenceValue());
    }

    @Override
    protected void onBlockMined(@NotNull GuildEventWrapper<BlockBreakEvent> wrapper) {
        doBlockHarvest(wrapper.getEvent().getBlock());
    }

    @Override
    protected void onCropHarvest(@NotNull GuildEventWrapper<HarvestCropEvent> wrapper) {
        doBlockHarvest(wrapper.getEvent().getBlock());
    }

    private void doBlockHarvest(@NotNull Block block) {
        int value = getEssenceValue(block);
        if (value < 1) return;

        spawnEssenceOrb(block.getLocation(), value);
    }

    private int getEssenceValue(@NotNull Block block) {
        return switch (block.getType()) {
            case PUMPKIN, MELON -> 3;
            case WHEAT, BEETROOTS, CARROTS, POTATOES -> 2;
            case SUGAR_CANE -> 1;
            default -> 0;
        };
    }

    @Override
    protected @NotNull String getIdentifier() {
        return "FARMING";
    }

    @Override
    protected @NotNull BarColor getBarColour() {
        return BarColor.YELLOW;
    }

    @Override
    public int getEssenceModelData() {
        return 102;
    }

}
