package me.mogubea.events;

import me.mogubea.items.MoguItem;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HarvestCropEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private final ItemStack itemStack;
    private final MoguItem moguItem;
    private final Block block;

    public HarvestCropEvent(@NotNull Player player, @NotNull Block block) {
        super(player);
        itemStack = player.getEquipment().getItemInMainHand();
        moguItem = MoguItem.from(itemStack);
        this.block = block;
    }

    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    public @Nullable MoguItem getMoguItem() {
        return moguItem;
    }

    public @NotNull Block getBlock() {
        return block;
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

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
