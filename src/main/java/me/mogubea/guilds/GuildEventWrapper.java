package me.mogubea.guilds;

import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class GuildEventWrapper<E extends Event> {

    private final PlayerProfile playerProfile;
    private final PlayerGuildData guildData;
    private final Player player;
    private final Main plugin;
    private final E event;

    public GuildEventWrapper(@NotNull Main plugin, @NotNull E event, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerProfile = PlayerProfile.from(player);
        this.event = event;

        this.guildData = playerProfile.getGuildData();
    }

    /**
     * @return The {@link Player}'s {@link PlayerProfile}.
     */
    protected @NotNull PlayerProfile getProfile() {
        return playerProfile;
    }

    protected @NotNull PlayerGuildData getGuildData() {
        return guildData;
    }

    protected @NotNull GuildData getGuildData(Guild guild) {
        return getGuildData().getGuildData(guild);
    }

    protected @NotNull Player getPlayer() {
        return player;
    }

    protected @NotNull Main getPlugin() { return plugin; }

    protected @NotNull E getEvent() {
        return event;
    }

    protected void setCancelled(boolean cancelled) {
        if (event instanceof Cancellable cancellable)
            cancellable.setCancelled(cancelled);
    }

    protected boolean isCancelled() {
        return (event instanceof Cancellable cancellable && cancellable.isCancelled());
    }

}
