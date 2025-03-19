package me.mogubea.guilds;

import me.mogubea.events.HarvestCropEvent;
import me.mogubea.events.MoguCropInteractEvent;
import me.mogubea.main.Main;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GuildManager {

    private final Main plugin;
    private final GuildDatasource datasource;
    private final List<Guild> guilds = new ArrayList<>();
    private final Map<String, Guild> guildsByIdentifier = new LinkedHashMap<>();

    private Guild[] guildArray;

    public GuildManager(@NotNull Main plugin) {
        this.plugin = plugin;
        this.datasource = new GuildDatasource(plugin, this);
    }

    /**
     * Fire all of the {@link Guild}s corresponding {@link Event} listeners.
     * @param event The {@link Event}.
     */
    public void callGuildEvent(@NotNull Event event) {
        callGuildEvent(event, guildArray);
    }

    /**
     * Fire the corresponding {@link Event} listener for the provided {@link Guild}(s).
     * @param event The {@link Event}.
     * @param guilds The {@link Guild}(s).
     */
    public void callGuildEvent(@NotNull Event event, Guild... guilds) {
        if (event instanceof Cancellable cancellable && cancellable.isCancelled()) return; // Disallow any cancelled events through here.

        if (event instanceof BlockBreakEvent e)
            for (int x = -1; ++x < guilds.length;)
                guilds[x].onBlockMined(new GuildEventWrapper<>(plugin, e, e.getPlayer()));
        else if (event instanceof HarvestCropEvent e)
            for (int x = -1; ++x < guilds.length;)
                guilds[x].onCropHarvest(new GuildEventWrapper<>(plugin, e, e.getPlayer()));
        else if (event instanceof MoguCropInteractEvent e)
            for (int x = -1; ++x < guilds.length;)
                guilds[x].onCustomCropInteract(new GuildEventWrapper<>(plugin, e, e.getPlayer()));
    }

    /**
     * Load the {@link Guilds} class. Must be called after the instantiation of the {@link GuildManager}.
     */
    public void loadGuilds() {
        Guilds.class.getClassLoader();
        guildArray = guilds.toArray(new Guild[0]);
    }

    public @NotNull GuildDatasource getDatasource() {
        return datasource;
    }

    /**
     * For use by the {@link Guilds} class.
     * @param guild A {@link Guild}.
     * @return A {@link Guild}.
     */
    protected @NotNull Guild registerGuild(@NotNull Guild guild) {
        this.guilds.add(guild);
        this.guildsByIdentifier.put(guild.getIdentifier(), guild);
        return guild;
    }

    protected @Nullable Guild getGuild(@NotNull String identifier) {
        return guildsByIdentifier.get(identifier);
    }

}
