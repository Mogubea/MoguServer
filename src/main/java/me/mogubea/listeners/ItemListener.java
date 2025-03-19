package me.mogubea.listeners;

import me.mogubea.main.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class ItemListener extends EventListener {

    protected ItemListener(Main plugin) {
        super(plugin);
    }

    /**
     * Prevent the ability for items to lose durability.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDurabilityLoss(PlayerItemDamageEvent e) {
        e.setCancelled(true);
    }

}