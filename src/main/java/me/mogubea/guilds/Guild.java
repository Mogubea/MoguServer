package me.mogubea.guilds;

import me.mogubea.entities.CustomEntityType;
import me.mogubea.events.HarvestCropEvent;
import me.mogubea.events.MoguCropInteractEvent;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public abstract class Guild {

    protected static final Random random = new Random();

    protected Guild() {

    }

    /**
     * @return The identifier used for stats relating to this Guild.
     */
    protected abstract @NotNull String getIdentifier();

    /**
     * @return The colour of the Bar when earning experience for this Guild.
     */
    protected abstract @NotNull BarColor getBarColour();

    public abstract int getEssenceModelData();

    protected void spawnEssenceOrb(@NotNull Location location, int value) {
        CustomEntityType.ESSENCE_ORB.spawn(location.add(random.nextDouble(0.2, 0.8), random.nextDouble(0.2, 0.8), random.nextDouble(0.2, 0.8)), orb -> {
            orb.setEssenceType(this);
            orb.setValue(value);
        });
    }

    protected void onBlockMined(@NotNull GuildEventWrapper<BlockBreakEvent> wrapper) {}

    protected void onCustomCropInteract(@NotNull GuildEventWrapper<MoguCropInteractEvent> wrapper) {}

    protected void onCropHarvest(@NotNull GuildEventWrapper<HarvestCropEvent> wrapper) {}

}
