package me.mogubea.profile;

import me.mogubea.items.ItemRarity;
import me.mogubea.main.Main;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerTeamManager {

	private final Main plugin;

	public PlayerTeamManager(Main plugin) {
		this.plugin = plugin;
	}

	public void onDisable() {
		// Clean up all entries from this run cycle
		for (Objective entry : plugin.getServer().getScoreboardManager().getMainScoreboard().getObjectives())
			entry.unregister();
	}
	
	public void initScoreboard(Player p) {
		createScoreboard(p);
		loadTeamsFor(p);
		updateTeam(p);
		PlayerProfile.from(p).getSidebar().refreshSidebar();
	}
	
	private void createScoreboard(Player p) {
		Scoreboard playerBoard = plugin.getServer().getScoreboardManager().getNewScoreboard();
//		Objective obj = playerBoard.registerNewObjective("showhealth", "health", Component.text("\u00a7c\u2764"));
//		obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
//		obj.getScore(p).setScore((int)p.getHealth());

		if (plugin.hasProtocolManager()) {
			for (ItemRarity rarity : ItemRarity.values()) {
				if (rarity.ordinal() < ItemRarity.UNCOMMON.ordinal()) continue;

				Team team = playerBoard.registerNewTeam("itemRarity_" + rarity.name());
				team.color(NamedTextColor.nearestTo(rarity.getColour()));
			}
		}

		p.setScoreboard(playerBoard);
	}
	
	/**
	 * Send an update to all online players to update their team information about this player.
	 */
	public void updateTeam(Player p) {
		final PlayerProfile pp = PlayerProfile.from(p);
		
//		List<String> flags = new ArrayList<>();
//		if (pp.isHidden()) flags.add("HIDE");
//		if (pp.isAFK()) flags.add("AFK");
//
//		Component pprefix = !flags.isEmpty() ? Component.text("" + flags, NamedTextColor.GRAY) : Component.empty();
//		final Component prefix = pprefix.append(pp.isRank(Rank.MODERATOR) ? Component.text("\u24E2 ", Rank.MODERATOR.getRankColour()) : flags.isEmpty() ? Component.empty() : Component.text(" "));
//		final Component suffix = pp.isRank(Rank.PLEBEIAN) ? Component.text(" \u2b50", pp.getDonorRank().getRankColour()) : Component.empty();
		final NamedTextColor color = NamedTextColor.nearestTo(pp.getNameColour());
		
		// This is required due to how scoreboards function per player
		// Team colouration is exclusive per scoreboard and must be redefined for every single player's scoreboard.
		// It is uncertain how memory intensive this can become the more players get online.. But thankfully Scoreboards are WeakReferenced.
		plugin.getServer().getOnlinePlayers().forEach((player) -> {
			String id = "id" + pp.getId() + "-" + PlayerProfile.from(player).getId();
			Team team = player.getScoreboard().getTeam(id);
			if (team == null) team = player.getScoreboard().registerNewTeam(id);
			if (!team.hasEntry(p.getName()))
				team.addEntry(p.getName());
			
			team.color(color);
//			team.prefix(prefix);
//			team.suffix(suffix);
		});
		
		// Cannot use p.teamDisplayName()
		p.playerListName(pp.getColouredName());
	}
	
	/**
	 * Generate a team for each of the currently online players
	 */
	private void loadTeamsFor(Player p) {
		plugin.getServer().getOnlinePlayers().forEach((player) -> {
			final PlayerProfile pp = PlayerProfile.from(player);
//			List<String> flags = new ArrayList<>();
//			if (pp.isHidden()) flags.add("HIDE");
//			if (pp.isAFK()) flags.add("AFK");
//
//			Component pprefix = !flags.isEmpty() ? Component.text("" + flags, NamedTextColor.GRAY) : Component.empty();
//			final Component prefix = pprefix.append(pp.isRank(Rank.MODERATOR) ? Component.text("\u24E2 ", Rank.MODERATOR.getRankColour()) : flags.isEmpty() ? Component.empty() : Component.text(" "));
//			final Component suffix = pp.isRank(Rank.PLEBEIAN) ? Component.text(" \u2b50", pp.getDonorRank().getRankColour()) : Component.empty();
			final NamedTextColor color = NamedTextColor.nearestTo(pp.getNameColour());
			
			String id = "id" + pp.getId() + "-" + PlayerProfile.from(p).getId();
			Team team = p.getScoreboard().getTeam(id);
			if (team == null) team = p.getScoreboard().registerNewTeam(id);
			if (!team.hasEntry(player.getName()))
				team.addEntry(player.getName());
			
			team.color(color);
//			team.prefix(prefix);
//			team.suffix(suffix);
		});
	}
	
}
