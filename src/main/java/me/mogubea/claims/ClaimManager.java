package me.mogubea.claims;

import me.mogubea.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.*;

public class ClaimManager {

    private final ClaimDatasource datasource;

    private final Map<Integer, Claim> claimsById = new HashMap<>();
    private final Map<World, Map<Long, Claim>> claimsByWorld = new HashMap<>();
    private final Map<Integer, List<Claim>> claimsByOwner = new HashMap<>();

    private final Map<Integer, ClaimTrustSettings> trustSettings = new HashMap<>();

    public ClaimManager(Main plugin) {
        this.datasource = new ClaimDatasource(plugin, this);

        for (World world : Bukkit.getWorlds())
            claimsByWorld.put(world, new LinkedHashMap<>());

        datasource.loadAll();
    }

    /**
     * Attempt to create a new {@link Claim}.
     * @param chunk The {@link Chunk} being claimed.
     * @param ownerId The owner.
     * @return The new {@link Claim}, otherwise null.
     */
    public @Nullable Claim createClaim(@NotNull Chunk chunk, int ownerId) {
        Claim claim = datasource.createClaim(chunk, ownerId);
        if (claim == null) return null;

        claimsByWorld.get(chunk.getWorld()).put(chunk.getChunkKey(), claim);
        claimsById.put(claim.getId(), claim);
        addToPlayerClaims(ownerId, claim);
        return claim;
    }

    /**
     * Load claim from database.
     */
    protected void loadClaim(int id, Chunk chunk, int ownerId, String name, Timestamp creationDate) {
        Claim claim = new Claim(this, id, ownerId, chunk, creationDate);
        claim.setName(name);
        claimsById.put(id, claim);
        claimsByWorld.get(chunk.getWorld()).put(chunk.getChunkKey(), claim);
        addToPlayerClaims(ownerId, claim);
    }

    /**
     * Attempt to delete an existing {@link Claim}.
     * @param claim The {@link Claim} being deleted.
     * @return Whether or not the deletion was successful.
     */
    public boolean deleteClaim(Claim claim) {
        if (datasource.deleteClaim(claim)) {
            claimsByWorld.get(claim.getWorld()).remove(claim.getChunk().getChunkKey());
            claimsById.remove(claim.getId());
            removeFromPlayerClaims(claim.getOwnerId(), claim);
            return true;
        }
        return false;
    }

    /**
     * Attempts to grab an existing {@link Claim} via their database id.
     * @param id The {@link Claim}'s database id.
     * @return The {@link Claim} with the specified id, otherwise null.
     */
    public @Nullable Claim getClaim(int id) {
        return claimsById.get(id);
    }

    public Claim getClaim(@NotNull Chunk chunk) {
        return claimsByWorld.get(chunk.getWorld()).get(chunk.getChunkKey());
    }

    public Claim getClaim(@NotNull Location location) {
        return getClaim(location.getChunk());
    }

    public boolean isClaimed(@NotNull Chunk chunk) {
        return getClaim(chunk) != null;
    }

    private void addToPlayerClaims(int ownerId, Claim claim) {
        if (!claimsByOwner.containsKey(ownerId))
            claimsByOwner.put(ownerId, new ArrayList<>());

        claimsByOwner.get(ownerId).add(claim);
    }

    private void removeFromPlayerClaims(int ownerId, Claim claim) {
        if (!claimsByOwner.containsKey(ownerId)) return;
        claimsByOwner.get(ownerId).remove(claim);
        if (claimsByOwner.get(ownerId).isEmpty())
            claimsByOwner.remove(ownerId);
    }

    public @NotNull List<Claim> getPlayerClaims(int ownerId) {
        return claimsByOwner.getOrDefault(ownerId, List.of());
    }

    public @NotNull List<Claim> getClaims() {
        return List.copyOf(claimsById.values());
    }

    public ClaimTrustSettings getTrustSettings(int playerId) {
        if (!trustSettings.containsKey(playerId))
            trustSettings.put(playerId, new ClaimTrustSettings(playerId, null));

        return trustSettings.get(playerId);
    }

    protected @NotNull List<ClaimTrustSettings> getAllTrustSettings() {
        return List.copyOf(trustSettings.values());
    }

}
