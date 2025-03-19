package me.mogubea.commands;

import me.mogubea.claims.Claim;
import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import me.mogubea.utils.LatinSmall;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandClaim extends MoguCommand {

	private final Map<Player, Chunk> pendingChunkClaim = new HashMap<>();

	public CommandClaim(Main plugin) {
		super(plugin, "mogu.cmd.claim", false, "claim");
		setDescription("Claim land.");

		getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(getPlugin(), () -> Map.copyOf(pendingChunkClaim).forEach((player, chunk) -> {
			if (!player.isOnline() || !PlayerProfile.from(player).onCooldown("pendingChunkClaim"))
				pendingChunkClaim.remove(player);

			getPlugin().visualiseChunk(player, chunk, Particle.SOUL_FIRE_FLAME);
		}), 20L, 20L);

	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		Player player = ((Player)sender);

		// Claim
		if (args.length == 0) {
			Claim claim = getPlugin().getClaimManager().getClaim(player.getChunk());
			if (claim != null) {
				getPlugin().visualiseChunk(player, claim.getChunk(), Particle.FLAME);
				throw new CommandException(player, "This chunk is already claimed " + (claim.getOwnerId() == profile.getId() ? "by you!" : "by another player!"));
			}

			player.sendMessage(Component.text("Please use ", NamedTextColor.GREEN).append(Component.text(LatinSmall.translate("/claim confirm"), NamedTextColor.GOLD).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/claim confirm")))
					.append(Component.text(" to confirm your claim. You have 20 seconds to do this.", NamedTextColor.GREEN)));
			pendingChunkClaim.put(player, player.getChunk());
			profile.addCooldown("pendingChunkClaim", 20000);
			return true;
		}

		String subCommand = args[0].toUpperCase();
		switch (subCommand) {
			case "CONFIRM" -> {
				if (!pendingChunkClaim.containsKey(player)) throw new CommandException(player, "You do not have a pending chunk claim.");
				Claim claim = getPlugin().getClaimManager().createClaim(pendingChunkClaim.get(player), profile.getId());
				pendingChunkClaim.remove(player);
				if (claim != null)
					player.sendMessage(Component.text("Successfully claimed this chunk!", NamedTextColor.GREEN));
				else
					player.sendMessage(Component.text("There was a problem with the creation of your claim. If this problem persists, please contact a member of staff.", NamedTextColor.RED));
			}
			case "CANCEL" -> {
				if (!pendingChunkClaim.containsKey(player)) throw new CommandException(player, "You do not have a pending chunk claim.");
				pendingChunkClaim.remove(player);
				player.sendMessage(Component.text("Cancelled pending chunk claim.", NamedTextColor.GREEN));
			}
		}


		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		return Collections.emptyList();
	}

}
