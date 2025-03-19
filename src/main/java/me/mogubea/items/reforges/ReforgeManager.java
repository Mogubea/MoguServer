package me.mogubea.items.reforges;

import me.mogubea.main.Main;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ReforgeManager {

    private final Main plugin;
    private final Map<String, Reforge> reforgesByName = new HashMap<>();
    private final Map<ReforgeTarget, List<Reforge>> reforgesByTarget = new HashMap<>();

    public ReforgeManager(Main plugin) {
        this.plugin = plugin;
    }

    public @Nullable Reforge generateReforge(@NotNull ItemStack itemStack) {
        List<ReforgeTarget> targets = ReforgeTarget.getApplicableTypes(itemStack);
        List<Reforge> validReforges = new ArrayList<>();

        for (int x = -1; ++x < targets.size();)
            validReforges.addAll(getReforges(targets.get(x)));
        validReforges.remove(getItemReforge(itemStack));

        if (validReforges.size() < 1) return null;

        double totalWeight = 0.0;
        for (int x = -1; ++x < validReforges.size();)
            totalWeight += validReforges.get(x).getWeight();

        int idx = -1;
        for (double r = Math.random() * totalWeight; ++idx < validReforges.size();) {
            r -= validReforges.get(idx).getWeight();
            if (r <= 0) break;
        }

        return validReforges.get(idx);
    }

    public void setItemReforge(@NotNull ItemStack itemStack, Reforge reforge) {
        itemStack.editMeta(meta -> {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (reforge == null)
                container.remove(Reforge.KEY_REFORGE);
            else
                container.set(Reforge.KEY_REFORGE, PersistentDataType.STRING, reforge.getIdentifier());
        });
    }

    public @Nullable Reforge getItemReforge(@NotNull ItemStack itemStack) {
        if (itemStack.getType().isEmpty() || !itemStack.getType().isItem()) return null;
        return getReforge(itemStack.getItemMeta().getPersistentDataContainer().get(Reforge.KEY_REFORGE, PersistentDataType.STRING));
    }

    public boolean hasReforge(@NotNull ItemStack itemStack, @NotNull Reforge reforge) {
        return getItemReforge(itemStack) == reforge;
    }

    public boolean isItemReforged(@NotNull ItemStack itemStack) {
        return getItemReforge(itemStack) != null;
    }

    @Contract("null -> null")
    public @Nullable Reforge getReforge(String identifier) {
        if (identifier == null) return null;
        return reforgesByName.get(identifier);
    }

    public @NotNull List<Reforge> getReforges(@NotNull ReforgeTarget target) {
        return reforgesByTarget.get(target);
    }

    public void registerReforges() {
        if (!reforgesByName.isEmpty()) return;

        for (ReforgeTarget target : ReforgeTarget.values())
            reforgesByTarget.put(target, new ArrayList<>());
        if (Reforges.DAMAGED != null)
            plugin.getSLF4JLogger().info("Successfully registered " + reforgesByName.size() + " reforges.");
        for (ReforgeTarget target : ReforgeTarget.values())
            reforgesByTarget.put(target, Collections.unmodifiableList(reforgesByTarget.get(target)));
    }

    protected Reforge register(@NotNull Reforge reforge) {
        if (reforgesByName.containsKey(reforge.getIdentifier()))
            throw new UnsupportedOperationException("A reforge with the identifier \"" + reforge.getIdentifier() + "\" already exists!");

        reforgesByName.put(reforge.getIdentifier(), reforge);
        for (int x = -1; ++x < reforge.getTargets().length;)
            reforgesByTarget.get(reforge.getTargets()[x]).add(reforge);

        return reforge;
    }

}
