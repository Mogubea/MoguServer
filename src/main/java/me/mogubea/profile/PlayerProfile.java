package me.mogubea.profile;

import me.mogubea.attributes.PlayerAttributes;
import me.mogubea.claims.Claim;
import me.mogubea.gui.MoguGui;
import me.mogubea.guilds.PlayerGuildData;
import me.mogubea.statistics.PlayerStatistics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class PlayerProfile {
    private static final TextColor DEFAULT_NAMECOLOUR = TextColor.color(0x7AFF8A);

    public static @Nullable PlayerProfile fromIfExists(int databaseId) {
        return PlayerProfileManager.getProfile(databaseId);
    }

    public static @NotNull PlayerProfile from(UUID uuid) {
        return PlayerProfileManager.getProfile(uuid);
    }

    public static @NotNull PlayerProfile from(Player player) {
        return PlayerProfileManager.getProfile(player.getUniqueId());
    }

    // Saved
    private final PlayerStatistics stats;
    private final PlayerGuildData guildData;
    private final PlayerRelations relations;

    private final int id;
    private String name;
    private String nickname;
    private final UUID uuid;
    private int nameGradientInt;
    private TextComponent colouredName;
    private TextComponent formattedName;
    private TextColor nameColour = DEFAULT_NAMECOLOUR;

    // Not Saved
    private final PlayerAttributes attributes;
    private final PlayerProfileManager manager;
    private final OfflinePlayer offlinePlayer;
    private Player player;
    private PlayerSidebar sidebar;
    private Claim currentClaim;
    private MoguGui currentGui;

    private final HashMap<String, Long> cooldowns = new HashMap<>();

    protected PlayerProfile(@NotNull PlayerProfileManager manager, int id, @NotNull UUID uuid, String nickname) {
        this.manager = manager;
        this.id = id;
        this.uuid = uuid;
        this.offlinePlayer = Bukkit.getOfflinePlayer(uuid);

        this.name = offlinePlayer.getName();
        this.nickname = nickname;

        this.attributes = new PlayerAttributes(this);

        this.stats = manager.getDatasource().loadPlayerStats(this);
        this.relations = manager.getDatasource().loadPlayerRelations(this);
        this.guildData = manager.getDatasource().loadGuildData(this);
    }

    /**
     * @return The player's UUID.
     */
    public @NotNull UUID getUniqueId() {
        return uuid;
    }

    /**
     * @return The player's database ID.
     */
    public int getId() {
        return id;
    }

    /**
     * @return The Player Profile Manager
     */
    public PlayerProfileManager getManager() {
        return manager;
    }

    /**
     * @return Whether the player is currently online.
     */
    public boolean isOnline() {
        this.player = offlinePlayer.getPlayer();
        return player != null;
    }

    /**
     * @return OfflinePlayer.
     */
    public @NotNull OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    /*
     * Names
     */

    /**
     * Set the user's true Mojang name.
     */
    public void setName(@NotNull String username) {
        this.name = username;
    }

    /**
     * @return The player's Mojang username.
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * @return The player's assigned nickname, otherwise null.
     */
    public @Nullable String getNickname() {
        return nickname;
    }

    /**
     * @return True if the player has a nickname different from their {@link #getName()}.
     */
    public boolean hasNickname() {
        return nickname != null;
    }

    /**
     * @return The player's {@link #getNickname()} if they have one. Otherwise, will return their {@link #getName()}.
     */
    public @NotNull String getDisplayName() {
        return nickname == null ? name : nickname;
    }

    /**
     * Update the player's visual nickname.
     * @param nickname The new nickname, or null.
     */
    public void setNickname(@Nullable String nickname) {
        this.nickname = nickname;
        updateDisplayedNames(true);
    }

    /**
     * @return The player's base name colour.
     */
    public TextColor getNameColour() {
        return nameColour;
    }

    /**
     * @return The player name's gradient being applied to their base colour.
     */
    public int getNameGradient() {
        return nameGradientInt;
    }

    /**
     * @return The player's coloured name.
     */
    public @NotNull TextComponent getColouredName() {
        if (colouredName == null)
            updateDisplayedNames(false);

        return colouredName;
    }

    /**
     * Set the player's name colour.
     * @param colour - Name colour in number form.
     */
    public void setNameColour(int colour) {
        this.nameColour = colour == 0 ? DEFAULT_NAMECOLOUR : TextColor.color(colour);
        updateDisplayedNames(false);
    }

    /**
     * Set the player's name gradient.
     * @param colour - Name gradient colour in number form.
     */
    public void setNameGradient(int colour) {
        this.nameGradientInt = colour;
        updateDisplayedNames(false);
    }

    /**
     * Set the player's name colour and gradient.
     * @param colour - Name colour in number form.
     * @param gradient - Name gradient colour in number form.
     */
    public void setNameColour(int colour, int gradient) {
        this.nameColour = colour == 0 ? DEFAULT_NAMECOLOUR : TextColor.color(colour);
        this.nameGradientInt = gradient;
        updateDisplayedNames(false);
    }

    /**
     * @return Whether the player has a custom name colour or not.
     */
    public boolean hasCustomNameColour() {
        return getNameColour() == DEFAULT_NAMECOLOUR;
    }

    /**
     * Update the player's tab name, team name, display name etc. in every place where it's necessary.
     * @param updateGameProfile - Whether to update the player's game profile or not.
     */
    public void updateDisplayedNames(boolean updateGameProfile) {
        this.colouredName = Component.text(nickname==null?name:nickname).color(nameColour);

        if (isOnline()) {
            if (updateGameProfile) {
                com.destroystokyo.paper.profile.PlayerProfile prof = offlinePlayer.getPlayerProfile();
                prof.setName(getDisplayName());
                player.setPlayerProfile(prof);
            }

            getManager().getTeamManager().updateTeam(player);
            player.displayName(getColouredName());
        }
    }

    /*
     * Player Stats
     */

    /**
     * @return The player's stats.
     */
    public @NonNull PlayerStatistics getStats() {
        return stats;
    }

    /*
     * Player relations
     */

    /**
     * @return The player's relationships.
     */
    public @NonNull PlayerRelations getRelationships() {
        return relations;
    }

    /*
     * Player cooldowns
     */

    /**
     * Set a cooldown to this player.
     * @param id The identifier of the cooldown
     * @param milli The length in milliseconds
     */
    public void addCooldown(String id, int milli) {
        this.cooldowns.put(id, System.currentTimeMillis() + milli);
    }

    /**
     * Remove a cooldown
     * @param id The identifier of the cooldown
     */
    public void clearCooldown(String id) {
        this.cooldowns.remove(id);
    }

    /**
     * Check if the player is on cooldown for the specified identifier
     * @param id The identifier of the cooldown
     * @return If the player is on cooldown
     */
    public boolean onCooldown(String id) {
        final long dura = this.cooldowns.getOrDefault(id, 0L);
        final boolean isCd = System.currentTimeMillis() < dura;
        if (dura > 0 && !isCd)
            this.cooldowns.remove(id);
        return isCd;
    }

    /**
     * Check if the player is on cooldown, and if not, put them on cooldown.
     * @param id The identifier of the cooldown
     * @param milli The length in milliseconds
     * @return If the player is on cooldown
     */
    public boolean onCdElseAdd(String id, int milli) {
        boolean onCd = onCooldown(id);
        if (!onCd)
            this.cooldowns.put(id, System.currentTimeMillis() + milli);
        return onCd;
    }

    /**
     * Get the remaining cooldown for the specified identifier
     * @param id The identifier of the cooldown
     * @return The time in milliseconds when this cooldown expires
     */
    public long getCooldown(String id) {
        return this.cooldowns.getOrDefault(id, 0L);
    }

    /*
     * Attributes
     */

    public @NotNull PlayerAttributes getAttributes() {
        return attributes;
    }


    /*
     * Claim detection
     */

    public @Nullable Claim getCurrentClaim() {
        return currentClaim;
    }

    public void setCurrentClaim(@Nullable Claim claim) {
        this.currentClaim = claim;
        getSidebar().updateClaim();
    }

    /*
     * Sidebar
     */

    public @NotNull PlayerSidebar getSidebar() {
        if (sidebar == null)
            sidebar = new PlayerSidebar(this);
        return sidebar;
    }

    /*
     * Guild Stuff
     */

    /**
     * @return The player's {@link PlayerGuildData}.
     */
    public @NotNull PlayerGuildData getGuildData() {
        return guildData;
    }

    /*
     * GUI Stuff
     */

    /**
     * @return The {@link MoguGui} currently being viewed by the player.
     */
    public @Nullable MoguGui getMoguGui() {
        if (!isOnline()) currentGui = null;
        return currentGui;
    }

    /**
     * Set the currently viewed {@link MoguGui}.
     * This method should only ever be called by the abstract {@link MoguGui}.
     * @param gui The {@link MoguGui}.
     */
    public void setMoguGui(@Nullable MoguGui gui) {
        if (!isOnline()) gui = null;
        this.currentGui = gui;
    }

}
