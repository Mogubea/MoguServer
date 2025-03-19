package me.mogubea.items.reforges;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public enum ReforgeTarget {

    DURABLE {
        @Override
        protected boolean canApply(@NotNull ItemStack itemStack) {
            return itemStack.getType().getMaxDurability() > 0 && itemStack.getType().getMaxStackSize() == 1;
        }
    },
    SWORDS {
        @Override
        protected boolean canApply(@NotNull ItemStack itemStack) {
            return itemStack.getType().name().endsWith("SWORD");
        }
    },
    PICKAXES {
        @Override
        protected boolean canApply(@NotNull ItemStack itemStack) {
            return itemStack.getType().name().endsWith("PICKAXE");
        }
    },
    AXES {
        @Override
        protected boolean canApply(@NotNull ItemStack itemStack) {
            return itemStack.getType().name().endsWith("AXE");
        }
    },
    SHOVELS {
        @Override
        protected boolean canApply(@NotNull ItemStack itemStack) {
            return itemStack.getType().name().endsWith("SHOVEL");
        }
    },
    HOES {
        @Override
        protected boolean canApply(@NotNull ItemStack itemStack) {
            return itemStack.getType().name().endsWith("HOE");
        }
    },
    TOOLS {
        @Override
        protected boolean canApply(@NotNull ItemStack itemStack) {
            return SHOVELS.canApply(itemStack) || AXES.canApply(itemStack) || PICKAXES.canApply(itemStack) || HOES.canApply(itemStack);
        }
    }
    ;

    protected static @NotNull List<ReforgeTarget> getApplicableTypes(@NotNull ItemStack itemStack) {
        List<ReforgeTarget> targets = new ArrayList<>();
        ReforgeTarget[] array = values();

        for (int x = -1; ++x < array.length;)
            if (array[x].canApply(itemStack))
                targets.add(array[x]);
        return targets;
    }

    protected abstract boolean canApply(@NotNull ItemStack itemStack);

}
