package me.mogubea.listeners;

import me.mogubea.entities.CustomEntityListener;
import me.mogubea.fishing.FishingListener;
import me.mogubea.main.Main;
import org.bukkit.plugin.PluginManager;

public class ListenerManager {

    public ListenerManager(Main plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(plugin.getBlockTracker(), plugin);
        pluginManager.registerEvents(new CustomEntityListener(plugin, plugin.getCustomEntityManager()), plugin);

        pluginManager.registerEvents(new StatListener(plugin), plugin);
        pluginManager.registerEvents(new SaveListener(plugin), plugin);
        pluginManager.registerEvents(new ChatListener(plugin), plugin);
        pluginManager.registerEvents(new ConnectionListener(plugin), plugin);
        pluginManager.registerEvents(new BlockListener(plugin), plugin);
        pluginManager.registerEvents(new VillagerListener(plugin), plugin);
        pluginManager.registerEvents(new ItemListener(plugin), plugin);
        pluginManager.registerEvents(new PlayerListener(plugin), plugin);
        pluginManager.registerEvents(new EntityListener(plugin), plugin);
        pluginManager.registerEvents(new FishingListener(plugin), plugin);
        pluginManager.registerEvents(new InventoryListener(plugin), plugin);
    }

}
