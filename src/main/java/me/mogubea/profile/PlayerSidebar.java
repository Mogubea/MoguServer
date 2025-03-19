package me.mogubea.profile;

import me.mogubea.claims.Claim;
import me.mogubea.statistics.SimpleStatType;
import me.mogubea.utils.Time;
import me.mogubea.utils.LatinSmall;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PlayerSidebar {

    private final Map<Integer, String> scoreboardLines = new HashMap<>();
    private final PlayerProfile profile;
    private Objective scoreboardObjective;

    protected PlayerSidebar(@NotNull PlayerProfile profile) {
        this.profile = profile;
    }

    public void refreshSidebar() {
        Player player = profile.getOfflinePlayer().getPlayer();
        if (player == null) return;

        Scoreboard scoreboard = player.getScoreboard();
        scoreboardLines.clear();

        if (scoreboard.getObjective("id" + profile.getId() + "-side") == null) {
            scoreboardObjective = scoreboard.registerNewObjective("id" + profile.getId() + "-side", Criteria.DUMMY, Component.text("TESTINGBAR", NamedTextColor.DARK_BLUE).decoration(TextDecoration.BOLD, true));
            scoreboardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        setScoreboardLine(20, "  ");
        setScoreboardLine(19, " \u00a71\u258E\u00a79 " + LatinSmall.translate("Information"));
        setScoreboardLine(18, " \u00a71\u258E\u00a77 " + LatinSmall.translate("Player: ") + "\u00a7f" + profile.getDisplayName());
        updatePlaytime();
        setScoreboardLine(16, " \u00a71\u258E\u00a77 " + LatinSmall.translate("Rank: ") + "\u00a7aMember");
        setScoreboardLine(15, "         ");
        updateClaim();
        updateTime();
//        setScoreboardLine(1, "\u00a77" + LatinSmall.translate(DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.now())));
        setScoreboardLine(0, "\u00a78" + LatinSmall.translate("beansbeans.net"));
    }

    protected void updateClaim() {
        Claim claim = profile.getCurrentClaim();
        if (claim != null) {
            String name = claim.getPlayerProfile() == null ? "No Owner" : claim.getPlayerProfile().getDisplayName();
            setScoreboardLine(14, " \u00a71\u258E\u00a79 " + LatinSmall.translate("Claim"));
            setScoreboardLine(13, " \u00a71\u258E\u00a77 " + LatinSmall.translate("Owner: ") + "\u00a7f" + name);
            setScoreboardLine(12, " \u00a71\u258E\u00a77 " + LatinSmall.translate("Trust: ") + "\u00a7b" + claim.getTrustLevel(profile.getId()));
            setScoreboardLine(11, "   ");
        } else {
            setScoreboardLine(14, null);
            setScoreboardLine(13, null);
            setScoreboardLine(12, null);
            setScoreboardLine(11, null);
        }
    }

    public void updateTime() {
        World world = Bukkit.getWorlds().get(0);
        int time = Time.getTime(world);
        int hour = (time+6000) / 1000;
        boolean night = hour < 6 || hour > 18;

        String symbol = night ? "\u00a79\u263d" : "\u00a7e\u2600";
        if (world.hasStorm()) {
            if (world.isThundering())
                symbol = night ? "\u00a79\u26a1" : "\u00a76\u26a1";
            else
                symbol = night ? "\u00a79\u2602" : "\u00a7b\u2602";
        }
        setScoreboardLine(1, symbol + "\u00a7f " + Time.getTimeString(time, true));
    }

    public void updatePlaytime() {
        setScoreboardLine(17, " \u00a71\u258E\u00a77 " + LatinSmall.translate("P.Time:") + "\u00a7b " + Time.millisToString(profile.getStats().getStat(SimpleStatType.GENERIC, "playtime") * 1000L, true));
    }

    private void setScoreboardLine(int line, String newContent) {
        String old = scoreboardLines.get(line);
        if (old != null) {
            if (old.equals(newContent)) return;
            scoreboardObjective.getScore(old).resetScore();
        }

        if (newContent != null) {
            scoreboardLines.put(line, newContent);
            scoreboardObjective.getScore(newContent).setScore(line);
        } else {
            scoreboardLines.remove(line);
        }
    }

}
