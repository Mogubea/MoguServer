package me.mogubea.claims;

import me.mogubea.claims.flags.MemberLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClaimTrustSettings {

    private final int id;
    private final Map<Integer, MemberLevel> trustLevels;
    private final Set<Integer> dirtySettings = new HashSet<>();

    protected ClaimTrustSettings(int id, @Nullable Map<Integer, MemberLevel> levels) {
        this.id = id;
        this.trustLevels = levels == null ? new HashMap<>() : levels;
    }

    public @NotNull MemberLevel getTrustLevel(int playerId) {
        return trustLevels.getOrDefault(playerId, MemberLevel.EVERYONE);
    }

    public void setTrustLevel(int playerId, @Nullable MemberLevel level) {
        if (trustLevels.get(playerId) == level) return;
        dirtySettings.add(playerId);

        if (level == null)
            trustLevels.remove(playerId);
        else
            trustLevels.put(playerId, level);
    }

    protected void loadTrustLevel(int playerId, @NotNull MemberLevel level) {
        trustLevels.put(playerId, level);
    }

    protected boolean hasDirtySettings() {
        return !dirtySettings.isEmpty();
    }

    protected Set<Integer> getDirtySettings() {
        return dirtySettings;
    }

    protected void clearDirtySettings() {
        dirtySettings.clear();
    }

    protected void addDirtyTrustSettings(List<Integer> list) {
        this.dirtySettings.addAll(list);
    }

    protected int getPlayerId() {
        return id;
    }

}
