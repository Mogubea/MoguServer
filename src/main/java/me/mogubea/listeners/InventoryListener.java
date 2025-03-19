package me.mogubea.listeners;

import me.mogubea.gui.MoguGui;
import me.mogubea.items.MoguItem;
import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class InventoryListener extends EventListener {

    protected InventoryListener(Main plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        PlayerProfile profile = PlayerProfile.from((Player) e.getViewers().get(0));
        MoguGui moguGui = profile.getMoguGui();
        if (moguGui == null) return;

        if (moguGui.preInventoryClick(e))
            moguGui.onInventoryClicked(e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent e) {
        PlayerProfile profile = PlayerProfile.from((Player) e.getViewers().get(0));
        MoguGui moguGui = profile.getMoguGui();
        if (moguGui == null) return;

        moguGui.onInventoryClosed(e);
        profile.setMoguGui(null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryDragEvent e) {
        PlayerProfile profile = PlayerProfile.from((Player) e.getViewers().get(0));
        MoguGui moguGui = profile.getMoguGui();
        if (moguGui == null) return;

        moguGui.onInventoryDrag(e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreItemCraft(PrepareItemCraftEvent e) {
        Recipe recipe = e.getRecipe();
        if (recipe == null || recipe.getResult().getType().isEmpty() || !(recipe.getResult().getType().isItem())) return;
        getPlugin().getItemManager().formatItemStack(recipe.getResult());
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemCraft(CraftItemEvent e) {
        ItemStack result = e.getInventory().getResult();
        if (result == null || result.getType().isEmpty()) return;
        if (result.getMaxStackSize() == 1) {
            getPlugin().getItemManager().addTrackingTags(result);
        } else {
            MoguItem moguItem = MoguItem.from(result);
            if (moguItem != null && moguItem.hasDupeProtection())
                getPlugin().getItemManager().addTrackingTags(result);
        }
    }

}
