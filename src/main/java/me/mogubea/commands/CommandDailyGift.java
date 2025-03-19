package me.mogubea.commands;

import me.mogubea.gui.MoguGuiDailyGifts;
import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class CommandDailyGift extends MoguCommand {

	public CommandDailyGift(Main plugin) {
		super(plugin, "mogu.cmd.gift", false, 0, "dailygift", "dailygifts", "gift", "gifts");
		setDescription("Claim your daily gifts!");
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		new MoguGuiDailyGifts((Player)sender).openInventory();
		return true;
	}

}
