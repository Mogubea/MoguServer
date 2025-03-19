package me.mogubea.gui;

import me.mogubea.statistics.SimpleStatType;
import me.mogubea.utils.CustomSkulls;
import me.mogubea.utils.LatinSmall;
import me.mogubea.utils.Time;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MoguGuiDailyGifts extends MoguGui {

    protected final static List<Integer> mapping = List.of(28, 29, 30, 31, 32, 33, 34, 20, 21, 22, 23, 24, 12, 13, 14, 4);
    private final static List<Component> defaultComponent = new ArrayList<>();

    static {
        defaultComponent.add(Component.text(LatinSmall.translate("daily playtime reward"), TextColor.color(0x26476E)).decoration(TextDecoration.ITALIC, false));
        defaultComponent.add(Component.empty());
        defaultComponent.add(Component.text("A small gift containing various goodies", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        defaultComponent.add(Component.text("for playing on the server today~", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        defaultComponent.add(Component.empty());
        defaultComponent.add(Component.empty());
    }

    public MoguGuiDailyGifts(@NotNull Player player) {
        super(player);

        setName(Component.text(negativeSpace(12) + "\u4E21" + negativeSpace(176), NamedTextColor.WHITE).append(Component.text("Daily Gifts", TextColor.color(0x3F3F3F))));
        this.presetSize = 36;
        this.presetInv = new ItemStack[presetSize];
        updateGiftItems(true);
    }

    @Override
    public void onInventoryClicked(@NotNull InventoryClickEvent e) {
        if (!e.isLeftClick()) return;
        if (!mapping.contains(e.getRawSlot())) return;
        int idx = mapping.indexOf(e.getRawSlot());

        if (getClaimStatus(idx) < 2) {
            player.playSound(player.getLocation(), Sound.BLOCK_CANDLE_EXTINGUISH, 0.6F, 0.4F);
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.4F);
        getViewedProfile().getStats().incrementStat(SimpleStatType.CLAIMED_GIFTS, getGiftIdentifier(idx));
        updateGiftItem(e.getRawSlot(), false);
    }

    @Override
    public void onTick() {
        updateGiftItems(false);
    }

    private void updateGiftItem(int slot, boolean first) {
        ItemStack currentItem = first ? CustomSkulls.PLAYTIME_GIFT.clone() : inventory.getItem(slot);
        int idx = mapping.indexOf(slot);

        Component lastLine = switch (getClaimStatus(idx)) {
            case 0 -> Component.text(" » Can be claimed in ", NamedTextColor.RED).append(Component.text(Time.millisToString((getRequirement(idx) - getDailyPlaytime()) * 1000L, false), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false);
            case 1 -> Component.text(" » Claimed!", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false);
            default -> Component.text(" » Unclaimed.", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false);
        };

        List<Component> lore = first ? new ArrayList<>(defaultComponent) : currentItem.lore();
        lore.set(5, lastLine);

        currentItem.editMeta(meta -> {
            if (first) meta.displayName(Component.text("Small Gift", TextColor.color(0x88E0FF)).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });

        if (first)
            presetInv[slot] = currentItem;
        else
            inventory.setItem(slot, currentItem);
    }

    private int getClaimStatus(int idx) {
        if (getDailyPlaytime() < getRequirement(idx)) {
            return 0;
        } else if (getViewedProfile().getStats().getDailyStat(SimpleStatType.CLAIMED_GIFTS, getGiftIdentifier(idx)) >= 1) {
           return 1;
        }
        return 2;
    }

    private void updateGiftItems(boolean first) {
        for (int slot : mapping)
            updateGiftItem(slot, first);
    }

    private int getDailyPlaytime() {
        return getViewedProfile().getStats().getDailyStat(SimpleStatType.GENERIC, "playtime");
    }

    private int getRequirement(int idx) {
        return idx * 60 * 10;
    }

    private @NotNull String getGiftIdentifier(int idx) {
        return "DAILY_PLAYTIME_GIFT_" + (idx+1);
    }

}
