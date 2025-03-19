package me.mogubea.guilds;

import org.bukkit.boss.BarColor;
import org.jetbrains.annotations.NotNull;

public class GuildMining extends Guild {

    @Override
    protected @NotNull String getIdentifier() {
        return "MINING";
    }

    @Override
    protected @NotNull BarColor getBarColour() {
        return BarColor.WHITE;
    }

    @Override
    public int getEssenceModelData() {
        return 100;
    }



}
