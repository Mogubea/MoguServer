package me.mogubea.events;

import me.mogubea.entities.MoguEntityCrop;
import me.mogubea.items.MoguItem;
import net.minecraft.world.InteractionResult;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoguCropInteractEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private boolean ignoreHarvesting;
    private InteractionResult result;
    private int consumeItemCount;
    private final ItemStack itemStack;
    private final MoguItem moguItem;
    private final MoguEntityCrop moguCrop;

    public MoguCropInteractEvent(@NotNull Player player, @NotNull MoguEntityCrop crop) {
        super(player);
        moguCrop = crop;
        itemStack = player.getEquipment().getItemInMainHand();
        moguItem = MoguItem.from(itemStack);

        ignoreHarvesting = false;
        result = InteractionResult.FAIL;
        consumeItemCount = 0;
    }

    public void setInteractionResult(boolean swing) {
        this.result = swing ? InteractionResult.SUCCESS : InteractionResult.FAIL; // Other Interaction Results have no effect on anything custom really.
    }

    public void setIgnoreHarvesting(boolean attemptHarvest) {
        this.ignoreHarvesting = attemptHarvest;
    }

    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    public @Nullable MoguItem getMoguItem() {
        return moguItem;
    }

    public @NotNull MoguEntityCrop getCrop() {
        return moguCrop;
    }

    public @NotNull InteractionResult getInteractionResult() {
        return result;
    }

    public boolean isIgnoringHarvest() {
        return ignoreHarvesting;
    }

    public boolean canHarvest() {
        return moguCrop.getAge() >= moguCrop.getMaxAge();
    }

    public void setConsumeItem() {
        setConsumeItem(1);
    }

    public void setConsumeItem(int amount) {
        consumeItemCount = amount;
    }

    public int getItemConsumeCount() {
        return consumeItemCount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @SuppressWarnings("unused") // It is used by Bukkit
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
