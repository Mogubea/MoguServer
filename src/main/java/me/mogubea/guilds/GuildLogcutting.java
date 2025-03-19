package me.mogubea.guilds;

import org.bukkit.boss.BarColor;
import org.jetbrains.annotations.NotNull;

public class GuildLogcutting extends Guild {

    @Override
    protected @NotNull String getIdentifier() {
        return "LOGCUTTING";
    }

    @Override
    protected @NotNull BarColor getBarColour() {
        return BarColor.GREEN;
    }

    @Override
    public int getEssenceModelData() {
        return 101;
    }
}
