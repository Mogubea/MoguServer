package me.mogubea.commands;

import me.mogubea.items.reforges.Reforge;
import me.mogubea.items.reforges.ReforgeManager;
import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandReforge extends MoguCommand {

	private final ReforgeManager reforgeManager;

	public CommandReforge(Main plugin) {
		super(plugin, "mogu.cmd.reforge", false, "reforge");
		setDescription("Reforge the item in your hand.");
//		addArgumentHelp("reforge", Component.text("Optional: The desired reforge."));

		reforgeManager = plugin.getItemManager().getReforgeManager();
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		Player player = (Player) sender;
		ItemStack itemStack = player.getEquipment().getItemInMainHand();
		if (!itemStack.getType().isItem() || itemStack.getType().isEmpty())
			itemStack = player.getEquipment().getItemInOffHand();
		if (!itemStack.getType().isItem() || itemStack.getType().isEmpty())
			throw new CommandException(player, "You cannot reforge the air.");

		switch (args.length >= 1 ? args[0].toLowerCase() : "") {
			case "get" -> {
				Reforge reforge = reforgeManager.getItemReforge(itemStack);
				if (reforge != null)
					player.sendMessage(Component.text("Your item has the " + reforge.getName() + " reforge.", NamedTextColor.GRAY));
				else
					player.sendMessage(Component.text("Your item doesn't have a reforge.", NamedTextColor.RED));
			}
			case "remove", "clear" -> {
				reforgeManager.setItemReforge(itemStack, null);
				player.sendMessage(Component.text("Your item is no longer reforged.", NamedTextColor.GREEN));
			}
			default -> {
				Reforge reforge = reforgeManager.generateReforge(itemStack);
				reforgeManager.setItemReforge(itemStack, reforge);
				player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.6F, 1.5F);
				if (reforge != null)
					player.sendMessage(Component.text("Your item now has the " + reforge.getName() + " reforge.", NamedTextColor.GREEN));
				else
					player.sendMessage(Component.text("Your item is no longer reforged.", NamedTextColor.GREEN));
			}
		}
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		return Collections.emptyList();
	}

}
