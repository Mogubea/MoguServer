package me.mogubea.listeners;

import me.mogubea.claims.flags.Flags;
import me.mogubea.events.HarvestCropEvent;
import me.mogubea.events.MoguCropInteractEvent;
import me.mogubea.guilds.Guilds;
import me.mogubea.items.ICustomCropInteract;
import me.mogubea.items.MoguItem;
import me.mogubea.items.MoguItemEdible;
import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import me.mogubea.statistics.SimpleStatType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerListener extends EventListener {

    protected PlayerListener(Main plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onRightClickPre(PlayerInteractEvent e) {
        if (e.getItem() == null) return;

        MoguItem moguItem = getPlugin().getItemManager().from(e.getItem());
        if (moguItem == null) return;

        moguItem.onInteract(e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null) return;

        Material material = block.getType();

        // Prevent Player Trampling
        if (e.getAction() == Action.PHYSICAL && material == Material.FARMLAND) {
            e.setCancelled(true);
            return;
        }

        ItemStack handItem = e.getItem();

        // Shear mushroom block faces
        if (e.getHand() != null && handItem != null && handItem.getType() == Material.SHEARS && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            switch(material) {
                case RED_MUSHROOM_BLOCK, BROWN_MUSHROOM_BLOCK, MUSHROOM_STEM -> {
                    BlockFace facing = e.getBlockFace();
                    MultipleFacing blockData = (MultipleFacing) block.getBlockData();
                    blockData.setFace(facing, !blockData.hasFace(facing));
                    block.setBlockData(blockData);

                    e.getPlayer().getWorld().spawnParticle(Particle.BLOCK_DUST, block.getLocation().add(0.5, 0.5, 0.5), 5, blockData);
                    e.getPlayer().getWorld().playSound(block.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_PUMPKIN_CARVE, 0.8F, 0.8F);
                    swingPlayerHand(e.getPlayer(), e.getHand() == EquipmentSlot.HAND);
                    return;
                }
            }
        }

        // Till Mycelium and Podzol
        if (e.getHand() != null && handItem != null && handItem.getType().name().endsWith("HOE") && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (material == Material.PODZOL || material == Material.MYCELIUM) {
                if (!enactClaimCheck(e, getClaim(block.getLocation()), e.getPlayer(), Flags.BUILD_ACCESS)) return;

                block.setType(Material.FARMLAND);
                e.getPlayer().getWorld().playSound(block.getLocation().add(0.5, 0.8, 0.5), Sound.ITEM_HOE_TILL, 1F, 1F);
                swingPlayerHand(e.getPlayer(), e.getHand() == EquipmentSlot.HAND);
                return;
            }
        }

        // Right click farming
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (!(block.getBlockData() instanceof Ageable ageable)) return;
        if (ageable.getAge() < ageable.getMaximumAge()) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        switch(material) {
            case WHEAT, CARROTS, POTATOES, BEETROOTS, NETHER_WART -> {
                if (!enactClaimCheck(e, getClaim(block.getLocation()), e.getPlayer(), Flags.CROP_HARVESTING)) return;
                HarvestCropEvent cropEvent = new HarvestCropEvent(e.getPlayer(), block);
                getPlugin().getServer().getPluginManager().callEvent(cropEvent);

                if (cropEvent.isCancelled()) return;

                for (ItemStack item : block.getDrops()) {
                    item.setAmount(Math.max(1, item.getAmount() - 1));
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }

                block.getWorld().playSound(block.getLocation().add(0.5, 0.5, 0.5), block.getBlockSoundGroup().getBreakSound(), 0.6F, 1F);
                block.getWorld().spawnParticle(Particle.BLOCK_DUST, block.getLocation().add(0.5, 0.15, 0.5), 3, 0.3, 0.1, 0.3, block.getBlockData());
                ageable.setAge(0);
                block.setBlockData(ageable);
                swingPlayerHand(e.getPlayer(), true);
                e.setCancelled(true);
            }
            case SWEET_BERRY_BUSH, CAVE_VINES -> {
                if (!enactClaimCheck(e, getClaim(block.getLocation()), e.getPlayer(), Flags.CROP_HARVESTING)) return;
                HarvestCropEvent cropEvent = new HarvestCropEvent(e.getPlayer(), block);
                getPlugin().getServer().getPluginManager().callEvent(cropEvent);
                if (cropEvent.isCancelled()) {
                    e.setCancelled(true);
                    return;
                }
            }
            default -> { return; }
        }

        PlayerProfile.from(e.getPlayer()).getStats().addToStat(SimpleStatType.CROP_HARVEST, material.name(), 1);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        // Lose half of your xp on death
        e.setNewTotalExp(e.getPlayer().getTotalExperience());
        e.setKeepInventory(true);
        e.getDrops().clear();
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent e) {
        if (getMoguItem(e.getItem()) instanceof MoguItemEdible edibleMogu)
            edibleMogu.onItemConsume(e);
    }

    @EventHandler
    public void onCustomCropInteract(MoguCropInteractEvent e) {
        if (e.getMoguItem() instanceof ICustomCropInteract cropInteractable)
            cropInteractable.onCustomCropInteract(e);

        getPlugin().getGuildManager().callGuildEvent(e, Guilds.FARMING);
    }

    @EventHandler
    public void onCropHarvest(HarvestCropEvent e) {
        getPlugin().getGuildManager().callGuildEvent(e, Guilds.FARMING);
    }

}
