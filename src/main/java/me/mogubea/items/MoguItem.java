package me.mogubea.items;

import me.mogubea.main.Main;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class MoguItem {

    private final MoguItemManager manager;

    private final String identifier;
    private final String displayName;
    private final Material material;
    private final ItemRarity rarity;

    private final ItemStack itemStack;

    private boolean enabled = true;
    private boolean tag = false;

    protected MoguItem(@NotNull MoguItemManager manager, @NotNull String identifier, @NotNull String displayName, @NotNull Material material, @NotNull ItemRarity rarity) {
        this.manager = manager;
        this.identifier = identifier;
        this.displayName = displayName;
        this.material = material;
        this.rarity = rarity;

        itemStack = new ItemStack(material);
        itemStack.editMeta(meta -> meta.getPersistentDataContainer().set(manager.KEY_ID, PersistentDataType.STRING, identifier));
    }

    protected void postCreation() {
        manager.formatItemStack(itemStack);
    }

    /**
     * @return The identifier of this item.
     */
    public @NotNull String getIdentifier() {
        return identifier;
    }

    /**
     * @return The display name of this item.
     */
    public @NotNull String getDisplayName() {
        return displayName;
    }

    /**
     * @return The material of this item.
     */
    public @NotNull Material getMaterial() {
        return material;
    }

    /**
     * @return The rarity of this item.
     */
    public @NotNull ItemRarity getRarity() {
        return rarity;
    }

    /**
     * @return The ItemStack
     */
    public @NotNull ItemStack getItemStack() {
        return getItemStack(1);
    }

    /**
     * @return The ItemStack
     */
    public @NotNull ItemStack getItemStack(int quantity) {
        if (hasDupeProtection() || quantity > itemStack.getMaxStackSize() || quantity < 1)
            quantity = 1;

        ItemStack itemStack = this.itemStack.clone();
        itemStack.setAmount(quantity);

        if (hasDupeProtection())
            manager.addTrackingTags(itemStack);

        return itemStack;
    }

    protected @NotNull MoguItem setCustomModelData(int data) {
        itemStack.editMeta(meta -> meta.setCustomModelData(data));
        return this;
    }

    /**
     * Give every instance of this item a UUID tag for the sake of protecting from dupes.
     * This item will be checked and compared against other items in every container in order to check for dupes.
     * This also enforces a stack size of 1.
     * @return The {@link MoguItem}.
     */
    protected @NotNull MoguItem enableDupeProtection() {
        this.tag = true;
        return this;
    }

    public boolean hasDupeProtection() {
        return tag;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected @NotNull Random getRandom() {
        return manager.getRandom();
    }

    protected @NotNull Main getPlugin() {
        return manager.getPlugin();
    }

    public void onInteract(@NotNull PlayerInteractEvent event) {}

    public static @Nullable MoguItem from(@NotNull ItemStack itemStack) {
        return MoguItemManager.fromStack(itemStack);
    }

    public static @NotNull String identifier(@NotNull ItemStack itemStack) {
        return MoguItemManager.getItemIdentifier(itemStack);
    }

}
