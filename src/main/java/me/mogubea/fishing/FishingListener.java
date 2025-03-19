package me.mogubea.fishing;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import me.mogubea.listeners.EventListener;
import me.mogubea.main.Main;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.HashMap;

public class FishingListener extends EventListener {

    private final HashMap<Player, FishingSession> fishingSession = new HashMap<>();

    public FishingListener(Main plugin) {
        super(plugin);

        startSessionLoop();
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent e) {
        Player player = e.getPlayer();
        FishHook hook = e.getHook();
        FishingSession session = fishingSession.get(player);

        switch (e.getState()) {
            // Successful pull
            case CAUGHT_FISH -> {
                boolean cancelEvent = true;

                // Initialise session
                if (session == null) {
                    session = new FishingSession(player, hook);
                    fishingSession.put(player, session);
                } else {
                    cancelEvent = !session.onSuccessfulReel();
                }

                e.setCancelled(cancelEvent);
            }
            // Non successful pull
            case REEL_IN -> {
                if (session == null || session.onFailedReel() <= 0F) return;
                e.setCancelled(true);
            }
            // Remove reel flag
            case FAILED_ATTEMPT -> {
                if (session == null) return;
                session.setBiting(false);
            }
            // If anything but a bite, end the fishing session
            case BITE -> {
                if (session == null) return;
                session.setBiting(true);
            }
            default -> fishingSession.remove(player);
        }
    }

    @EventHandler
    public void onBobDespawn(EntityRemoveFromWorldEvent e) {
        if (e.getEntity() instanceof FishHook hook && hook.getShooter() instanceof Player owner)
            fishingSession.remove(owner);
    }

    private void startSessionLoop() {
        getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(getPlugin(), () -> fishingSession.forEach((player, session) -> session.onSessionTick()), 20L, 20L);
    }

}
