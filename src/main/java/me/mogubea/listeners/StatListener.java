package me.mogubea.listeners;

import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import me.mogubea.statistics.PlayerStatistics;
import me.mogubea.statistics.SimpleStatType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class StatListener extends EventListener {

    protected StatListener(Main plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        PlayerProfile profile = PlayerProfile.from(e.getPlayer());
        PlayerStatistics stats = profile.getStats();
        String blockId = e.getBlock().getType().key().value().toUpperCase();

        stats.addToStat(SimpleStatType.BLOCK_BREAK, blockId, 1);
        stats.addToStat(SimpleStatType.BLOCK_BREAK, "TOTAL", 1);

        if (isBlockNatural(e.getBlock())) {
            stats.addToStat(SimpleStatType.NATURAL_BLOCK_BREAK, blockId, 1);
            stats.addToStat(SimpleStatType.NATURAL_BLOCK_BREAK, "TOTAL", 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        PlayerProfile profile = PlayerProfile.from(e.getPlayer());
        PlayerStatistics stats = profile.getStats();

        stats.addToStat(SimpleStatType.BLOCK_PLACE, e.getBlock().getType().key().value().toUpperCase(), 1);
        stats.addToStat(SimpleStatType.BLOCK_BREAK, "TOTAL", 1);
    }


}
