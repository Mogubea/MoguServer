package me.mogubea.commands;

import me.mogubea.items.MoguItem;
import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import me.mogubea.statistics.SimpleStatType;
import me.mogubea.utils.LatinSmall;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.*;

/**
 * A custom command
 */
public abstract class MoguCommand implements TabExecutor {

	private final Main plugin;
	protected final Random random = new Random();
	protected final String permissionString;
	protected final boolean canConsoleRun;
	protected final String[] aliases;

	protected final Map<String, Component> argumentHelp = new LinkedHashMap<>();

	protected final int minArgs;

	private Component usageComponent;
	private String description = "";
	private int cooldown;

	private boolean enabled = true;

	public MoguCommand(final Main plugin, String... aliases) {
		this(plugin, "", false, 0, aliases);
	}

	public MoguCommand(final Main plugin, boolean canConsoleRun, String... aliases) {
		this(plugin, "", canConsoleRun, 0, aliases);
	}

	public MoguCommand(final Main plugin, boolean canConsoleRun, int minArguments, String... aliases) {
		this(plugin, "", canConsoleRun, minArguments, aliases);
	}

	public MoguCommand(final Main plugin, String permissionString, boolean canConsoleRun, @NotNull String... aliases) {
		this(plugin, permissionString, canConsoleRun, 0, aliases);
	}

	public MoguCommand(final Main plugin, String permissionString, boolean canConsoleRun, int minArguments, @NotNull String... aliases) {
		this.plugin = plugin;
		this.permissionString = permissionString;
		this.canConsoleRun = canConsoleRun;
		this.aliases = aliases;
		this.minArgs = minArguments;
	}
	
	public abstract boolean runCommand(@Nullable PlayerProfile profile, @NotNull CommandSender sender, @NotNull Command cmd, @NotNull String str, @NotNull String[] args);
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String str, @NotNull String[] args) {
		try {
			if (str.startsWith("moguserver:"))
				str = str.substring(11);
			
			if (!this.isEnabled())
				throw new CommandException(sender, "/"+ str + " is currently disabled.");
		
			if (!this.canConsoleRun && !isPlayer(sender))
				throw new CommandException(sender, "You must be in-game to use /"+str+".");

			PlayerProfile profile = isPlayer(sender) ? PlayerProfile.from((Player) sender) : null;

			// Check for cool-downs if they are a player
			if (profile != null) {
				if (profile.onCooldown("cmd." + cmd.getName())) {
//					sender.sendActionBar(Component.text("Please wait " + Utils.timeStringFromNow(profile.getCooldown("cmd."+cmd.getName())) + " before using /"+str+" again.", NamedTextColor.RED));
					return false;
				}

				if (profile.onCdElseAdd("cmd", 600)) {
					sender.sendActionBar(Component.text("You are sending commands too fast!", NamedTextColor.RED));
					return false;
				}

				profile.getStats().addToStat(SimpleStatType.COMMAND, cmd.getName(), 1);
				profile.getStats().addToStat(SimpleStatType.COMMAND, "TOTAL", 1);
			}

			if (args.length < minArgs)
				throw new CommandException(sender, getUsage(sender, str.toLowerCase(), args));

			boolean successful = runCommand(isPlayer(sender) ? PlayerProfile.from((Player)sender) : null, sender, cmd, str.toLowerCase(), args);

			// Add command cooldown after a successful run.
			if (successful && profile != null && cooldown > 0)
				profile.addCooldown("cmd." + cmd.getName(), cooldown);

			return successful;
		} catch (CommandException e) {
			e.notifySender();
		}

		return false;
	}
	
	@Override
	public final @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String str, @NotNull String[] args) {
		if (!this.canConsoleRun && !(sender instanceof Player))
			return new ArrayList<>();
		
		String[] newArgs = new String[args.length];
		for (int x = 0; x < args.length; x++)
			newArgs[x] = args[x].toLowerCase();
		
		return runTabComplete(sender, cmd, str, newArgs);
	}

	protected @Nullable List<String> runTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String str, @NotNull String[] args) {
		return Collections.emptyList();
	}

	protected @NotNull Component getUsage(@NotNull CommandSender sender, @NotNull String str, @NotNull String[] args) {
		if (usageComponent != null) return usageComponent;

		Component usage = Component.text("Usage: ", NamedTextColor.RED).append(Component.text("/" + LatinSmall.translate(str), NamedTextColor.GOLD).hoverEvent(Component.text(getDescription())));
		int loops = 0;

		for (Map.Entry<String, Component> argHelp : argumentHelp.entrySet()) {
			if (++loops <= argumentHelp.size())
				usage = usage.append(Component.text(" ")); // Add space

			if (args.length > loops) {
				usage = usage.append(Component.text(LatinSmall.translate(args[loops]), NamedTextColor.GOLD).hoverEvent(argHelp.getValue()));
			} else {
				usage = usage.append(Component.text("<", NamedTextColor.GOLD)
						.append(Component.text(LatinSmall.translate(argHelp.getKey()), NamedTextColor.YELLOW).append(Component.text(">", NamedTextColor.GOLD)))
						.hoverEvent(argHelp.getValue()));
			}
		}

		return usageComponent = usage;
	}

	/**
	 * Add some argument help that is shown to players when using a command incorrectly.
	 * @param argument The argument name
	 * @param help The help
	 */
	protected void addArgumentHelp(String argument, Component help) {
		this.argumentHelp.put(argument, help);
	}

	/**
	 * Set the description of this {@link MoguCommand}
	 * @param description The desired description
	 */
	protected void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Set the usage cooldown of this {@link MoguCommand}
	 * @param millis The desired cooldown in milliseconds
	 * @return The {@link MoguCommand}
	 */
	protected @NotNull MoguCommand setCooldown(int millis) {
		this.cooldown = millis;
		return this;
	}

	public String getPermissionString() {
		return permissionString;
	}

	public @NotNull String getDescription() {
		if (description == null)
			description = "Mogu command.";
		return description;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public String[] getAliases() {
		return aliases;
	}

	public @NotNull String getName() {
		return aliases[0];
	}

	@NotNull
	public Main getPlugin() {
		return plugin;
	}

	@NotNull
	protected Random getRandom() {
		return random;
	}
	
	protected boolean isPlayer(@NotNull CommandSender sender) {
		return sender instanceof Player;
	}

	protected @NotNull TabCompleter getTabCompleter() {
		return getPlugin().getCommandManager().getTabCompleter();
	}

	/**
	 * Attempt to obtain an {@link ItemStack} from the provided String.
	 * @param sender The {@link CommandSender}.
	 * @param str The string being converted.
	 * @param amount The stack size of the item.
	 * @return A valid {@link ItemStack} of the desired size.
	 */
	protected @NotNull ItemStack toItemStack(@NotNull CommandSender sender, String str, int amount) {
		final MoguItem bi = getPlugin().getItemManager().from(str);
		
		ItemStack i;
		if (bi == null) {
			try {
				Material m = Material.valueOf(str.toUpperCase());
				if (!m.isItem() || m.isEmpty())
					throw new CommandException(sender, "\"" + str + "\" is an invalid item.");
//				i = BeanItem.formatItem(new ItemStack(m));
				i = new ItemStack(m);
			} catch (IllegalArgumentException e) {
				throw new CommandException(sender, "The item \"" + str + "\" does not exist.");
			}
		} else {
			i = bi.getItemStack();
		}
		
		i.setAmount(amount); 
		return i;
	}
	
	protected Component toHover(@NotNull ItemStack item) {
		if (!item.getType().isItem() || item.getType().isEmpty()) return Component.translatable(item);
		
		ItemMeta meta = item.getItemMeta();
		Component displayName = meta.hasDisplayName() ? meta.displayName() : Component.translatable(item);

		assert displayName != null;
		return displayName.hoverEvent(item.asHoverEvent());
	}
	
	protected int toIntDef(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException numberformatexception) {
            return def;
        }
    }
	
	protected int toInt(CommandSender sender, String s, String err) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException numberformatexception) {
            throw new CommandException(sender, err);
        }
    }

	protected float toFloat(CommandSender sender, String s) {
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException numberformatexception) {
			throw new CommandException(sender, "'" + s + "' is an invalid floating number.");
		}
	}
	
	protected int toIntMinMax(CommandSender sender, String s, int i, int j) {
        int number = toInt(sender, s, "'" + s + "' is an invalid number.");
		if (number < i || number > j)
			throw new CommandException(sender, "Please specify a number between " + i + " and " + j + ".");

		return number;
    }
	
	protected Player toPlayer(CommandSender sender, String s) {
		return toPlayer(sender, s, true);
	}
	
	protected Player toPlayer(CommandSender sender, String s, boolean self) {
		Player p = getPlugin().searchForPlayer(s);
		if (p == null)
			throw new CommandException(sender, "Couldn't find player '"+s+"'");
		if (!self && p == sender)
			throw new CommandException(sender, "You can't target yourself!");
		
		return p;
	}
	
//	protected PlayerProfile toProfile(CommandSender sender, String s) {
//		PlayerProfile pp = PlayerProfile.fromIfExists(s);
//		if (pp == null) {
//			Player p = toPlayer(sender, s);
//			if (p != null)
//				pp = PlayerProfile.from(p);
//		}
//
//		return pp;
//	}
	
	protected Component toName(CommandSender sender) {
		if (sender instanceof Player)
			return PlayerProfile.from(((Player)sender)).getColouredName();
		else
			return Component.text("Server", NamedTextColor.RED);
	}
	
	protected World toWorld(CommandSender sender, String s) {
		World w = Bukkit.getWorld(s);
		if (w == null)
			throw new CommandException(sender, "Couldn't find world '"+s+"'");
		return w;
	}
	
	protected Collection<? extends Player> onlinePlayers() {
		return Bukkit.getOnlinePlayers();
	}
	
//	protected Collection<? extends Player> onlineRank(Rank rank) {
//		ArrayList<Player> players = new ArrayList<>();
//		for (Player p : onlinePlayers())
//			if (PlayerProfile.from(p).isRank(rank))
//				players.add(p);
//		return players;
//	}
	
//	protected boolean isRank(CommandSender sender, Rank rank) {
//		return !isPlayer(sender) || PlayerProfile.from((Player) sender).isRank(rank);
//	}

//	@NotNull
//	protected Rank getRank(CommandSender sender) {
//		return isPlayer(sender) ? PlayerProfile.from((Player)sender).getHighestRank() : Rank.OWNER;
//	}
	
//	protected boolean checkRankPower(CommandSender sender, Player target, String err) {
//		if (sender == target)
//			return true;
//		if (getPlugin().permissionManager().isPreviewing(target)) // Prevent bs
//			return false;
//		final boolean ra = !isPlayer(sender) || getRank(sender).power() > getRank(target).power();
//		if (!ra)
//			throw new CommandException(sender, err);
//		return true;
//	}
	
	protected boolean checkPlayer(CommandSender sender) {
		if (!isPlayer(sender))
			throw new CommandException(sender, "You must be in-game to do that.");
		return true;
	}
	
	protected boolean checkSubPerm(CommandSender sender, String subCmd) {
		if (getPermissionString() == null || getPermissionString().isEmpty()) return true;
		final boolean ra = sender.hasPermission("*") || sender.hasPermission(getPermissionString() + "." + subCmd) || sender.hasPermission(getPermissionString() + ".*");
		if (!ra) 
			throw new CommandException(sender, "You don't have permission to use /" + aliases[0] + " " + subCmd + ".");
		return true;
	}
	
	protected boolean isSafe(Location loc) {
		return loc.getBlock().getType() != Material.LAVA && (!loc.subtract(0, 0.2, 0).getBlock().isEmpty());
	}
	
	protected boolean noGM(CommandSender sender, GameMode mode) {
		if (sender instanceof Player && ((Player)sender).getGameMode() == mode)
			throw new CommandException(sender, "This command is disabled in " + mode.name().toLowerCase() + " mode.");
		return true;
	}
	
	protected final DecimalFormat df = new DecimalFormat("#,###");
	protected final DecimalFormat dec = new DecimalFormat("#,###.##");
	
}
