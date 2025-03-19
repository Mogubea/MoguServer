package me.mogubea.listeners;

import me.mogubea.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldSaveEvent;

public class SaveListener extends EventListener {

    protected SaveListener(Main plugin) {
        super(plugin);
    }

    @EventHandler
    public void onServerSave(WorldSaveEvent e) {
        if (e.getWorld().getEnvironment() == World.Environment.NETHER) {
            if (getPlugin().isEnabled())
                Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> getPlugin().saveAll());
            else
                getPlugin().saveAll();
        }
    }

}
