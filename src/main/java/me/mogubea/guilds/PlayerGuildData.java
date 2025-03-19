package me.mogubea.guilds;

import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PlayerGuildData {

    private final PlayerProfile profile;
    private final Map<Guild, GuildData> guildData = new HashMap<>();

    protected final BossBar xpBar = Bukkit.getServer().createBossBar("", BarColor.WHITE, BarStyle.SOLID);

    private int barScheduler;

    public PlayerGuildData(@NotNull PlayerProfile profile) {
        this.profile = profile;
        xpBar.setVisible(false);
    }

    protected void loadGuildData(@NotNull Map<Guild, GuildData> guildDataMap) {
        this.guildData.putAll(guildDataMap);
    }

    protected @NotNull Map<Guild, GuildData> getDataMap() {
        return Map.copyOf(guildData);
    }

    /**
     * Get the player's individual {@link GuildData} for the specified {@link Guild}.
     * @param guild The {@link Guild}.
     * @return The player's {@link GuildData} for this {@link Guild}.
     */
    public @NotNull GuildData getGuildData(@NotNull Guild guild) {
        GuildData data = guildData.get(guild);
        if (data == null) {
            data = new GuildData(this, guild);
            guildData.put(guild, data);
        }
        return data;
    }

    /**
     * Get the player's individual {@link Guild} level for the specified {@link Guild}.
     * @param guild The {@link Guild}.
     * @return The player's level in this {@link Guild}.
     */
    public int getGuildLevel(@NotNull Guild guild) {
        return getGuildData(guild).getLevel();
    }

    public boolean hasGuildStone(@NotNull Guild guild) {
        return getGuildData(guild).hasStone();
    }

    public void addExperience(@NotNull Guild guild, int experience, @NotNull ExperienceSource source) {
        GuildData guildData = getGuildData(guild);

        guildData.addExperience(experience, source);

        xpBar.setColor(guild.getBarColour());
        xpBar.setProgress(Math.min(1, (float)guildData.getExperience() / (float)guildData.getTotalExperience()));
        xpBar.setTitle(guild.getIdentifier() + ", " + guildData.getExperience() + "/" + guildData.getTotalExperience());
        xpBar.setVisible(true);

        if (barScheduler != 0) Bukkit.getScheduler().cancelTask(barScheduler);
        barScheduler = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> xpBar.setVisible(false), 50L);
    }

    protected @NotNull PlayerProfile getProfile() {
        return profile;
    }

    public void refreshPlayer() {
        Player player = profile.getOfflinePlayer().getPlayer();
        if (player != null)
            xpBar.addPlayer(player);
    }

}
