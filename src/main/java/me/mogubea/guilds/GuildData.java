package me.mogubea.guilds;

import me.mogubea.statistics.PlayerStatistics;
import me.mogubea.statistics.SimpleStatType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class GuildData {

    private static final byte MAX_LEVEL = 20;
    private static final ArrayList<Long> xpRequirements = new ArrayList<>();

    static {
        for (int x = -1; ++x < MAX_LEVEL + 1;)
            xpRequirements.add(x * x * 250L + x * 10000L);
    }

    private final PlayerGuildData parent;
    private final Guild guild;

    // Saved
    private int totalGuildExp;
    private int currency;

    private byte amuletSlot;
    private boolean hasStone;
    private int stoneEssence;

    // Non Essential Saves
    private int guildLevel;
    private int guildExp;

    // Not Saved
    private boolean dirty;

    protected GuildData(PlayerGuildData data, Guild guild) {
        this(data, guild, 0, 0, 0, false, (byte) -1);
    }

    protected GuildData(PlayerGuildData data, Guild guild, int exp, int essence, int currency, boolean hasStone, byte slot) {
        this.parent = data;
        this.guild = guild;
        this.totalGuildExp = exp;
        this.stoneEssence = essence;
        this.currency = currency;
        this.hasStone = hasStone;
        this.amuletSlot = slot;
        calculateGuildLevel();
    }

    /**
     * @return Get the owning {@link PlayerGuildData}.
     */
    protected @NotNull PlayerGuildData getParent() {
        return parent;
    }

    /**
     * @return Get this {@link GuildData}'s {@link Guild} type.
     */
    protected @NotNull Guild getGuild() {
        return guild;
    }

    public void giveCurrency(int amount) {
        this.currency += amount;
    }

    public int getCurrency() {
        return currency;
    }

    /*
     * Amulet
     */

    protected void setAmuletSlot(byte slot) {
        this.amuletSlot = slot;
        this.dirty = true;
    }

    public byte getAmuletSlot() {
        return amuletSlot;
    }

    /*
     * Experience and Levels
     */

    /**
     * Give guild experience to the player.
     * @param experience The amount of experience.
     * @param source The {@link ExperienceSource} of the experience.
     */
    protected void addExperience(int experience, @Nullable ExperienceSource source) {
        this.guildExp += experience;
        this.totalGuildExp += experience;
        this.dirty = true;

        if (guildLevel >= MAX_LEVEL) return;
        if (guildExp < xpRequirements.get(guildLevel)) return;

        // TODO: Level up sequence
        guildExp -= xpRequirements.get(guildLevel);
        guildLevel++;

        if (source != null) {
            getStats().addToStat(SimpleStatType.GUILD, "XP_" + guild.getIdentifier()+"_" + source.getIdentifier(), experience);
            getStats().addToStat(SimpleStatType.GUILD, "XP_" + guild.getIdentifier()+"_TOTAL", experience);
            getStats().addToStat(SimpleStatType.GUILD, "XP_TOTAL_" + source.getIdentifier(), experience);
            getStats().addToStat(SimpleStatType.GUILD, "XP_TOTAL", experience);
        }
    }

    /**
     * @return The player's {@link Guild} level.
     */
    public int getLevel() {
        return guildLevel;
    }

    /**
     * @return The amount of {@link Guild} experience on this current {@link Guild} level.
     */
    public int getExperience() {
        return guildExp;
    }

    /**
     * @return The total amount of {@link Guild} experience that the player has collected.
     */
    public int getTotalExperience() {
        return totalGuildExp;
    }

    private void calculateGuildLevel() {
        long xp = totalGuildExp;
        byte lvl = 0;
        long xpReq = xpRequirements.get(lvl);

        while(xp>=(xpReq) && lvl < MAX_LEVEL) {
            lvl++;
            xp-=xpReq;
            xpReq = xpRequirements.get(lvl);
        }

        guildLevel = lvl;
        this.guildExp = (int) xp;
    }

    /*
     * Essence
     */

    /**
     * Give stone essence to the player.
     * @param essence The amount of essence.
     */
    public void addEssence(int essence) {
        if (!hasStone) return;

        this.stoneEssence += essence;
        this.dirty = true;

        getStats().addToStat(SimpleStatType.GUILD, "ESSENCE_" + guild.getIdentifier(), essence);
        getStats().addToStat(SimpleStatType.GUILD, "ESSENCE_TOTAL", essence);
        getParent().getProfile().getOfflinePlayer().getPlayer().sendMessage(Component.text("nerd " + getEssence() + " (+" + essence + " " + getGuild().getIdentifier() + " essence)"));
    }

    public int getEssence() {
        return stoneEssence;
    }

    public void setHasStone(boolean stone) {
        if (stone != hasStone)
            this.dirty = true;
        if (!stone)
            this.setAmuletSlot((byte)-1);
        this.hasStone = stone;
    }

    public boolean hasStone() {
        return hasStone;
    }

    /*
     * Dirty
     */

    protected boolean isDirty() {
        return dirty;
    }

    protected void setClean() {
        this.dirty = false;
    }

    private @NotNull PlayerStatistics getStats() {
        return parent.getProfile().getStats();
    }

}
