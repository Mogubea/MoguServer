package me.mogubea.listeners;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import me.mogubea.claims.flags.Flags;
import me.mogubea.entities.MoguEntityCrop;
import me.mogubea.items.MoguItem;
import me.mogubea.items.MoguItemBlock;
import me.mogubea.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;

public class BlockListener extends EventListener {

    public BlockListener(Main plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPreBlockPlace(BlockPlaceEvent e) {
        // Prevent placing a block on top of a crop holding block
        if (e.getBlock().getLocation().subtract(0, 1, 0).getBlock().hasMetadata(MoguEntityCrop.METADATA_TAG)) {
            e.setCancelled(true);
            return;
        }

        // After custom crop block check for immersion's sake
        enactClaimCheck(e, getClaim(e.getBlock().getLocation()), e.getPlayer(), Flags.BUILD_ACCESS);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        MoguItem moguItem = getPlugin().getItemManager().from(e.getItemInHand());
        if (moguItem instanceof MoguItemBlock moguItemBlock)
            moguItemBlock.onBlockPlace(e);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPreBlockBreak(BlockBreakEvent e) {
        enactClaimCheck(e, getClaim(e.getBlock().getLocation()), e.getPlayer(), Flags.BUILD_ACCESS);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        removeFloatingCustomCrops(e.getBlock());
        getPlugin().getGuildManager().callGuildEvent(e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreakByBlock(BlockBreakBlockEvent e) {
        removeFloatingCustomCrops(e.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (int x = -1; ++x < e.getBlocks().size();)
            removeFloatingCustomCrops(e.getBlocks().get(x));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (int x = -1; ++x < e.getBlocks().size();)
            removeFloatingCustomCrops(e.getBlocks().get(x));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDestroy(BlockDestroyEvent e) {
        removeFloatingCustomCrops(e.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent e) {
        if (e.getBlock().getType() == Material.FARMLAND && e.getBlock().hasMetadata(MoguEntityCrop.METADATA_TAG))
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplosion(BlockExplodeEvent e) {
        if (getClaim(e.getBlock().getLocation()) != null) {
            e.setCancelled(true);
            return;
        }

        for (int x = -1; ++x < e.blockList().size();)
            removeFloatingCustomCrops(e.blockList().get(x));
    }

    private void removeFloatingCustomCrops(Block block) { // TODO: Attempt to detect the player that caused the crop to break and grant XP if necessary
        // Destroy custom blocks that are being held up by this block
        if (block.getType() == Material.FARMLAND && block.hasMetadata(MoguEntityCrop.METADATA_TAG)) {
            block.removeMetadata(MoguEntityCrop.METADATA_TAG, getPlugin());
            block.getLocation().add(0.5, 1, 0.5).getNearbyEntitiesByType(ArmorStand.class, 0.2).forEach(armorStand -> {
                if (!armorStand.getPersistentDataContainer().isEmpty())
                    armorStand.remove();
            });
        }
    }

}
