package me.mogubea.guilds;

import me.mogubea.main.Main;

public class Guilds {

    /**
     * The excavators
     */
    public static final Guild MINING;
    public static final Guild LOG_CUTTING;
    public static final Guild FARMING;

    static {
        GuildManager manager = Main.getInstance().getGuildManager();

        MINING = manager.registerGuild(new GuildMining());
        LOG_CUTTING = manager.registerGuild(new GuildLogcutting());
        FARMING = manager.registerGuild(new GuildFarming());
    }

}
