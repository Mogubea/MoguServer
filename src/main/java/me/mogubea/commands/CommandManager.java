package me.mogubea.commands;

import me.mogubea.main.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * A simple command manager in charge of registering and unregistering custom commands without
 * the need to constantly update the plugin.yml
 */
public class CommandManager {

	private final List<Command> myCommands = new ArrayList<>();
	private Map<String, MoguCommand> moguCommands = new HashMap<>();
	
	private final Main plugin;
	private final TabCompleter tabCompleter;
	
	public CommandManager(Main plugin) {
		this.plugin = plugin;
		this.tabCompleter = new TabCompleter();
		long millis = System.currentTimeMillis();

		registerCommand(new CommandI(plugin));
		registerCommand(new CommandClaim(plugin));
		registerCommand(new CommandDailyGift(plugin));
		registerCommand(new CommandReforge(plugin));

		moguCommands = Map.copyOf(moguCommands);
		plugin.getSLF4JLogger().info("Registered " + moguCommands.size() + " commands in " + (System.currentTimeMillis()-millis) + "ms");
	}
	
//	/**
//	 * Unregisters /reload, /rl, /pl and /plugins as they get in the way of my own commands.
//	 */
//	private void unregisterAnnoyingBukkit() {
//		try {
//			SimplePluginManager spm = (SimplePluginManager) getPlugin().getServer().getPluginManager();
//			Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
//			commandMapField.setAccessible(true);
//
//			SimpleCommandMap scm = (SimpleCommandMap) commandMapField.get(spm);
//			scm.getKnownCommands().remove("reload");
//			scm.getKnownCommands().remove("rl");
//			scm.getKnownCommands().remove("pl");
//			scm.getKnownCommands().remove("plugins");
//			commandMapField.setAccessible(false);
//		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Register a {@link MoguCommand} to the server.
	 * @param moguCommand The {@link MoguCommand} instance.
	 */
	private void registerCommand(MoguCommand moguCommand) {
		String[] aliases = moguCommand.getAliases();
		String cmdName = aliases[0];

		if (moguCommands.containsKey(cmdName))
			throw new RuntimeException("The command /" + cmdName + " has already been registered.");

		try {
			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
			PluginCommand command = c.newInstance(cmdName, plugin);

			command.setAliases(Arrays.asList(aliases));
			command.setPermission(moguCommand.getPermissionString());
			command.permissionMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
			command.setDescription(moguCommand.getDescription());
			command.setExecutor(moguCommand);
			Bukkit.getCommandMap().register(plugin.getName(), command);

			myCommands.add(command);
			moguCommands.put(cmdName, moguCommand);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Unregister all the custom commands.
	 */
	public void unregisterCommands() {
		int size = getMyCommands().size();
		for (int x = -1; ++x < size;)
			myCommands.get(x).unregister(Bukkit.getCommandMap());
	}

	public @NotNull List<Command> getMyCommands() {
		return myCommands;
	}

	/**
	 * Grab an unmodifiable map of the custom {@link MoguCommand}s.
	 */
	public @NotNull Map<String, MoguCommand> getMoguCommands() {
		return moguCommands;
	}

	public @NotNull TabCompleter getTabCompleter() {
		return tabCompleter;
	}

}
