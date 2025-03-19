package me.mogubea.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class MoguItemExperienceJar extends MoguItemBlock {

    private final NamespacedKey KEY_STORED_XP;

    protected MoguItemExperienceJar(@NotNull MoguItemManager manager, @NotNull String identifier, @NotNull String displayName, @NotNull Material material, @NotNull ItemRarity rarity) {
        super(manager, identifier, displayName, material, rarity);

        this.KEY_STORED_XP = new NamespacedKey(manager.getPlugin(), "STORED_XP");
    }

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        int storedXp = item.getItemMeta().getPersistentDataContainer().getOrDefault(KEY_STORED_XP, PersistentDataType.INTEGER, 0);
        int toEdit = Math.min(player.isSneaking() ? 50 : 10, player.getTotalExperience());

        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK, RIGHT_CLICK_AIR -> { // Store xp
                player.setTotalExperience(player.getTotalExperience() - toEdit);
                item.editMeta(meta -> meta.getPersistentDataContainer().set(KEY_STORED_XP, PersistentDataType.INTEGER, (storedXp + toEdit)));
            }
            case LEFT_CLICK_BLOCK, LEFT_CLICK_AIR -> { // Retrieve xp

            }
        }



        event.setCancelled(true);
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

}
