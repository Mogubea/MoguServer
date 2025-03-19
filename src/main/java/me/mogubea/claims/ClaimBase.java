package me.mogubea.claims;

import me.mogubea.claims.flags.Flag;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public abstract class ClaimBase {

    protected final ClaimManager manager;

    private final int id;
    private final World world;

    protected final Map<Flag<?>, Object> flags = new HashMap<>();
    private final Set<Flag<?>> pendingSaves = new HashSet<>();

    protected ClaimBase(@NotNull ClaimManager manager, int id, @NotNull World world) {
        this.manager = manager;
        this.id = id;
        this.world = world;
    }

    public int getId() {
        return this.id;
    }

    public World getWorld() {
        return world;
    }

    public Map<Flag<?>, Object> getFlags() {
        return this.flags;
    }

    public <T extends Flag<V>, V> V setFlag(@NotNull T flag, @Nullable V val) {
        if (val == flags.get(flag)) return val;
        pendingSaves.add(flag);

        if (val == null)
            flags.remove(flag);
        else
            flags.put(flag, flag.validateValue(val));

        flag.onUpdate(this);
        return val;
    }

    protected <T extends Flag<V>, V> void loadFlag(@NotNull T flag, @NotNull V val) {
        flags.put(flag, flag.validateValue(val));
    }

    public <T extends Flag<V>, V> boolean hasFlag(T flag) {
        return flags.containsKey(flag);
    }

    @SuppressWarnings("unchecked")
    public @Nullable <T extends Flag<V>, V> V getFlag(@NotNull T flag) {
        return (V) flags.get(flag);
    }

    public @NotNull <T extends Flag<V>, V> V getEffectiveFlag(@NotNull T flag) {
        V val = getFlag(flag);
        return val == null ? flag.getDefault() : val;
    }

    protected boolean hasDirtyFlags() {
        return !pendingSaves.isEmpty();
    }

    protected Set<Flag<?>> getDirtyFlags() {
        return pendingSaves;
    }

    protected void clearDirtyFlags() {
        pendingSaves.clear();
    }

    protected void addDirtyFlags(Collection<Flag<?>> dirtyFlags) {
        this.pendingSaves.addAll(dirtyFlags);
    }

}
