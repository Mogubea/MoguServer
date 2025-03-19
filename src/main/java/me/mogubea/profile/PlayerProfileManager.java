package me.mogubea.profile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.mogubea.main.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public class PlayerProfileManager {

    private final PlayerProfileDatasource datasource;
    private final PlayerTeamManager teamManager;

    private int statDay; // The amount of days passed since equinox

    private final LoadingCache<UUID, PlayerProfile> profileCache = CacheBuilder.from("maximumSize=500,expireAfterAccess=6m").build(
            new CacheLoader<>() {
                public @NotNull PlayerProfile load(@NotNull UUID playerUUID) throws Exception {
                    return datasource.getOrCreateProfile(playerUUID);
                }
            });

    public PlayerProfileManager(Main plugin) {
        this.datasource = new PlayerProfileDatasource(plugin, this);
        this.teamManager = new PlayerTeamManager(plugin);
        this.statDay = (int) Math.floorDiv(System.currentTimeMillis(), 86400000);
    }

    public void onEnable(Main plugin) {
        // /reload precaution
        for (Player player : plugin.getServer().getOnlinePlayers())
            teamManager.initScoreboard(player);
    }

    public void onDisable() {
        teamManager.onDisable();
    }

    protected PlayerProfileDatasource getDatasource() {
        return datasource;
    }

    public PlayerTeamManager getTeamManager() {
        return teamManager;
    }

    protected LoadingCache<UUID, PlayerProfile> getCache() {
        return profileCache;
    }

    public Collection<PlayerProfile> getLoadedProfiles() {
        return profileCache.asMap().values();
    }

    public boolean isProfileLoaded(int id) {
        UUID uuid = datasource.getUUIDfromId(id);
        if (uuid == null) return false;
        return isProfileLoaded(uuid);
    }

    public boolean isProfileLoaded(UUID uuid) {
        return profileCache.getIfPresent(uuid) != null;
    }

    public @NotNull PlayerProfile getPlayerProfile(@NotNull UUID uuid) {
        return getPlayerProfile(uuid, null);
    }

    /**
     * Attempts to get or create a {@link PlayerProfile} for the provided player {@link UUID}.
     * @return The {@link PlayerProfile} associated with the provided player {@link UUID}.
     * @throws RuntimeException - If there was a problem getting or creating a {@link PlayerProfile} for this player.
     */
    public @NotNull PlayerProfile getPlayerProfile(@NotNull UUID uuid, @Nullable String username) {
        try {
            // If a username is not provided or the cache already has an entry, just grab the entry.
            if (username == null || profileCache.getIfPresent(uuid) != null)
                return profileCache.get(uuid);

            // If a username is provided and the cache doesn't have an entry, go through the precautionary creation method, which requires the username.
            PlayerProfile profile = datasource.getOrCreateProfile(uuid, username);
            profileCache.put(uuid, profile);
            return profile;
        } catch (Exception e) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.getPlayer() != null)
                offlinePlayer.getPlayer().kick(Component.text("There was an error loading your Player Profile.", NamedTextColor.RED));
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Attempts to get a {@link PlayerProfile} from the specified database id.
     * @return The {@link PlayerProfile} associated with the provided player id or null.
     * @throws RuntimeException - If there was a problem getting or creating a {@link PlayerProfile} for this player.
     */
    protected @Nullable PlayerProfile getPlayerProfile(int databaseId) {
        UUID uuid = datasource.getUUIDfromId(databaseId);
        if (uuid == null) return null;
        return getPlayerProfile(uuid);
    }

    public int getStatDay() {
        return statDay;
    }

    public boolean checkForDayChange() {
        if (getDatasource().isCurrentlySaving()) return false; // Disallow change if profiles are currently saving as a precaution.

        int day = (int) Math.floorDiv(System.currentTimeMillis(), 86400000);
        boolean change = statDay != day;

        if (change) {
            getDatasource().saveAllPlayerStats();
            statDay = day;
        }

        return change;
    }

    /**
     * Helper static method for {@link PlayerProfileManager#getPlayerProfile(UUID)}.
     * @return The {@link PlayerProfile} associated with the provided player {@link UUID}.
     * @throws RuntimeException - If there was a problem getting or creating a {@link PlayerProfile} for this player.
     */
    protected static @NotNull PlayerProfile getProfile(@NotNull OfflinePlayer p) {
        return getProfile(p.getUniqueId());
    }

    /**
     * Helper static method for {@link PlayerProfileManager#getPlayerProfile(UUID)}.
     * @return The {@link PlayerProfile} associated with the provided player {@link UUID}.
     * @throws RuntimeException - If there was a problem getting or creating a {@link PlayerProfile} for this player.
     */
    protected static @NotNull PlayerProfile getProfile(@NotNull UUID uuid) {
        return Main.getInstance().getProfileManager().getPlayerProfile(uuid);
    }

    protected static @Nullable PlayerProfile getProfile(int databaseId) {
        return Main.getInstance().getProfileManager().getPlayerProfile(databaseId);
    }

}
