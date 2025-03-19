package me.mogubea.claims;

import me.mogubea.claims.flags.FlagMember;
import me.mogubea.claims.flags.MemberLevel;
import me.mogubea.profile.PlayerProfile;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;

public class Claim extends ClaimBase {

    private final int ownerId;
    private final Timestamp creationDate;

    private final int chunkX;
    private final int chunkZ;

    private String name;

    protected Claim(ClaimManager manager, int id, int ownerId, Chunk chunk) {
        this(manager, id, ownerId, chunk, new Timestamp(System.currentTimeMillis()));
    }

    protected Claim(ClaimManager manager, int id, int ownerId, Chunk chunk, Timestamp creationDate) {
        super(manager, id, chunk.getWorld());
        this.ownerId = ownerId;
        this.creationDate = creationDate;
        this.chunkX = chunk.getX();
        this.chunkZ = chunk.getZ();
    }

    public int getOwnerId() {
        return ownerId;
    }

    public @Nullable PlayerProfile getPlayerProfile() {
        return PlayerProfile.fromIfExists(ownerId);
    }

    public Chunk getChunk() {
        return getWorld().getChunkAt(chunkX, chunkZ);
    }

    public void setName(String name) {
        this.name = name;
    }

    public @NotNull MemberLevel getTrustLevel(int playerId) {
        if (playerId == ownerId)
            return MemberLevel.OWNER;
        if (ownerId < 1)
            return MemberLevel.EVERYONE;
        return manager.getTrustSettings(ownerId).getTrustLevel(playerId);
    }

    public @NotNull MemberLevel getTrustLevel(Player player) {
        return getTrustLevel(PlayerProfile.from(player).getId());
    }

    public boolean isTrustedFor(Player player, FlagMember flag) {
        return getTrustLevel(player).is(getEffectiveFlag(flag));
    }

}
