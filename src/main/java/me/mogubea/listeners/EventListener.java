package me.mogubea.listeners;

import me.mogubea.claims.Claim;
import me.mogubea.claims.flags.FlagMember;
import me.mogubea.items.MoguItem;
import me.mogubea.main.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EventListener implements Listener {

    protected final Main plugin;

    protected EventListener(Main plugin) {
        this.plugin = plugin;
    }

    protected Main getPlugin() {
        return plugin;
    }

    /**
     * @param block The block being checked whether it is natural or not.
     * @return Whether the block is natural or not.
     */
    protected boolean isBlockNatural(Block block) {
        return plugin.getBlockTracker().isBlockNatural(block);
    }

    /**
     * Swing the player's main or offhand.
     * @param player The player
     * @param main If true, main hand, otherwise offhand.
     */
    protected void swingPlayerHand(Player player, boolean main) {
        if (main) {
            player.swingMainHand();
        } else {
            player.swingOffHand();
        }
    }

    protected @Nullable MoguItem getMoguItem(@NotNull ItemStack itemStack) {
        return plugin.getItemManager().from(itemStack);
    }

    protected boolean doClaimCheck(@Nullable Claim claim, @NotNull Player player, @NotNull FlagMember flag) {
        if (claim == null) return true;
        return claim.isTrustedFor(player, flag);
    }

    protected boolean enactClaimCheck(@NotNull Cancellable event, @Nullable Claim claim, @NotNull Player player, @NotNull FlagMember flag) {
        if (!doClaimCheck(claim, player, flag)) {
            event.setCancelled(true);
            player.sendActionBar(Component.text("You cannot do that here.", NamedTextColor.RED));
            return false;
        }
        return true;
    }

    protected @Nullable Claim getClaim(@NotNull Location location) {
        return plugin.getClaimManager().getClaim(location.getChunk());
    }

}
