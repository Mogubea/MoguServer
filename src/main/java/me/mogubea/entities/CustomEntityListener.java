package me.mogubea.entities;

import me.mogubea.listeners.EventListener;
import me.mogubea.main.Main;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.EntitiesLoadEvent;

public class CustomEntityListener extends EventListener {

    private final CustomEntityManager manager;

    public CustomEntityListener(Main plugin, CustomEntityManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(EntitiesLoadEvent e) {
        for (int x = -1; ++x < e.getEntities().size();) {
            Entity entity = e.getEntities().get(x);
            if (entity.getPersistentDataContainer().isEmpty()) continue;
            if (((CraftEntity)entity).getHandle() instanceof IMoguEntity) continue;

            manager.tryToConvert(entity);
        }
    }

}
