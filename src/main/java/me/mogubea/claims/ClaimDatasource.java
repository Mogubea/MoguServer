package me.mogubea.claims;

import me.mogubea.claims.flags.Flag;
import me.mogubea.claims.flags.Flags;
import me.mogubea.claims.flags.MemberLevel;
import me.mogubea.data.PrivateDatasource;
import me.mogubea.main.Main;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;

public class ClaimDatasource extends PrivateDatasource {

    private final String claimsTable = getDatasourceConfig().getString("tables.claims.main");
    private final String flagTable = getDatasourceConfig().getString("tables.claims.flags");
    private final String trustTable = getDatasourceConfig().getString("tables.claims.trust");

    private final ClaimManager manager;

    protected ClaimDatasource(Main plugin, ClaimManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @Override
    public void loadAll() {
        try {
            loadClaims();
            loadClaimFlags();
            loadClaimTrustSettings();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAll() {
        saveDirtyFlags();
        saveClaimTrustSettings();
    }

    /**
     * Attempt to create a claim in the database
     * @param chunk The chunk being claimed
     * @param ownerId The owner of the chunk
     * @return The created {@link Claim}, otherwise null.
     */
    protected @Nullable Claim createClaim(@NotNull Chunk chunk, int ownerId) {
        ResultSet rs = null;
        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + claimsTable + " (worldUUID, chunkX, chunkZ, ownerId) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            int idx = 0;
            s.setString(++idx, chunk.getWorld().getUID().toString());
            s.setInt(++idx, chunk.getX());
            s.setInt(++idx, chunk.getZ());
            s.setInt(++idx, ownerId);
            s.executeUpdate();

            rs = s.getGeneratedKeys();
            if (rs.next())
                return new Claim(manager, rs.getInt(1), ownerId, chunk, new Timestamp(System.currentTimeMillis()));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
        }
        return null;
    }

    /**
     * Attempt to delete a claim from the database
     * @param claim The claim being deleted
     * @return Whether the deletion was successful.
     */
    protected boolean deleteClaim(@NotNull Claim claim) {
        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("DELETE FROM " + claimsTable + " c JOIN " + flagTable + " f ON c.id = f.id WHERE id = ?")) {
            s.setInt(1, claim.getId());
            s.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Load all the Claims.
     */
    private void loadClaims() throws SQLException {
        try(Connection c = getNewConnection(); ResultSet rs = c.prepareStatement("SELECT * FROM " + claimsTable).executeQuery()) {
            while(rs.next()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(rs.getString("worldUUID"));
                } catch (IllegalArgumentException e) {
                    continue;
                }

                World world = getPlugin().getServer().getWorld(uuid);
                if (world == null) continue;

                Chunk chunk = world.getChunkAt(rs.getInt("chunkX"), rs.getInt("chunkZ"));

                manager.loadClaim(rs.getInt("id"), chunk, rs.getInt("ownerId"), rs.getString("name"), rs.getTimestamp("claimTime"));
            }
        }
    }

    /*
     * CLAIM FLAGS
     */

    /**
     * Load all the Claim flags.
     */
    @SuppressWarnings("unchecked")
    private <T extends Flag<V>, V> void loadClaimFlags() throws SQLException {
        try(Connection c = getNewConnection(); ResultSet rs = c.prepareStatement("SELECT * FROM " + flagTable).executeQuery()) {
            while(rs.next()) {
                Claim claim = manager.getClaim(rs.getInt("id"));
                if (claim == null) continue;

                T flag = (T) Flags.getFlag(rs.getString("flag"));
                if (flag == null) continue;

                claim.loadFlag(flag, (V)rs.getObject("value"));
            }
        }
    }

    private void saveDirtyFlags() {
        Map<Claim, List<Flag<?>>> toWrite = new HashMap<>(); // Flags that need to be inserted or updated
        Map<Claim, List<Flag<?>>> toDelete = new HashMap<>(); // Flags that need to be deleted
        List<Claim> claimsWithChanges = new ArrayList<>();
        List<Claim> claims = manager.getClaims();

        for (int x = -1; ++x < claims.size();) {
            Claim claim = claims.get(x);
            if (!claim.hasDirtyFlags()) continue;

            claimsWithChanges.add(claim);
            toWrite.put(claim, new ArrayList<>());
            toDelete.put(claim, new ArrayList<>());

            for (Flag<?> flag : claim.getDirtyFlags()) {
                if (claim.hasFlag(flag))
                    toWrite.get(claim).add(flag);
                else
                    toDelete.get(claim).add(flag);
            }

            claim.clearDirtyFlags();
        }

        try {
            updateDirtyFlags(toWrite);
            removeDirtyFlags(toDelete);
        } catch (SQLException e) {
            e.printStackTrace();

            // Restore all pending changes upon save failure
            for (int x = -1; ++ x < claimsWithChanges.size();) {
                Claim claim = claimsWithChanges.get(x);
                toWrite.get(claim).addAll(toDelete.get(claim));
                claim.addDirtyFlags(toWrite.get(claim));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Flag<V>, V> void updateDirtyFlags(Map<Claim, List<Flag<?>>> toUpdate) throws SQLException {
        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + flagTable + " (id,flag,value) VALUES (?,?,?) ON DUPLICATE KEY UPDATE value = VALUES(value)")) {
            for (Map.Entry<Claim, List<Flag<?>>> entry : toUpdate.entrySet()) {
                Claim claim = entry.getKey();
                List<Flag<?>> flags = entry.getValue();

                for (int x = -1; ++x < flags.size();) {
                    T flag = (T) flags.get(x);

                    s.setInt(1, claim.getId());
                    s.setString(2, flag.getIdentifier());
                    s.setString(3, flag.marshal(claim.getFlag(flag)));
                    s.addBatch();
                }
            }

            s.executeBatch();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Flag<V>, V> void removeDirtyFlags(Map<Claim, List<Flag<?>>> toRemove) throws SQLException {
        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("DELETE FROM " + flagTable + " WHERE id = ? AND flag = ?")) {
            for (Map.Entry<Claim, List<Flag<?>>> entry : toRemove.entrySet()) {
                Claim claim = entry.getKey();
                List<Flag<?>> flags = entry.getValue();

                for (int x = -1; ++x < flags.size();) {
                    T flag = (T) flags.get(x);

                    s.setInt(1, claim.getId());
                    s.setString(2, flag.getIdentifier());
                    s.addBatch();
                }
            }

            s.executeBatch();
        }
    }

    /*
     * TRUST SETTINGS
     */

    private void loadClaimTrustSettings() throws SQLException {
        try(Connection c = getNewConnection(); ResultSet rs = c.prepareStatement("SELECT * FROM " + trustTable).executeQuery()) {
            while(rs.next()) {
                try {
                    MemberLevel trustLevel = MemberLevel.valueOf(rs.getString("trustLevel").toUpperCase());
                    manager.getTrustSettings(rs.getInt("id")).loadTrustLevel(rs.getInt("targetId"), trustLevel);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void saveClaimTrustSettings() {
        Map<ClaimTrustSettings, List<Integer>> toWrite = new HashMap<>();
        Map<ClaimTrustSettings, List<Integer>> toDelete = new HashMap<>();
        List<ClaimTrustSettings> listsWithChanges = new ArrayList<>();
        List<ClaimTrustSettings> lists = manager.getAllTrustSettings();

        for (int x = -1; ++x < lists.size();) {
            ClaimTrustSettings trustSettings = lists.get(x);
            if (!trustSettings.hasDirtySettings()) continue;

            listsWithChanges.add(trustSettings);
            toWrite.put(trustSettings, new ArrayList<>());
            toDelete.put(trustSettings, new ArrayList<>());

            for (int id : trustSettings.getDirtySettings()) {
                if (trustSettings.getTrustLevel(id).is(MemberLevel.MEMBER))
                    toWrite.get(trustSettings).add(id);
                else
                    toDelete.get(trustSettings).add(id);
            }

            trustSettings.clearDirtySettings();
        }

        try {
            updateDirtyTrustSettings(toWrite);
            removeDirtyTrustSettings(toDelete);
        } catch (SQLException e) {
            e.printStackTrace();

            // Restore all pending changes upon save failure
            for (int x = -1; ++ x < listsWithChanges.size();) {
                ClaimTrustSettings trustSettings = listsWithChanges.get(x);
                toWrite.get(trustSettings).addAll(toDelete.get(trustSettings));
                trustSettings.addDirtyTrustSettings(toWrite.get(trustSettings));
            }
        }

    }

    private void updateDirtyTrustSettings(Map<ClaimTrustSettings, List<Integer>> toUpdate) throws SQLException {
        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + trustTable + " (id,targetId,trustLevel) VALUES (?,?,?) ON DUPLICATE KEY UPDATE trustLevel = VALUES(trustLevel)")) {
            for (Map.Entry<ClaimTrustSettings, List<Integer>> entry : toUpdate.entrySet()) {
                ClaimTrustSettings trustSettings = entry.getKey();
                List<Integer> ids = entry.getValue();

                for (int x = -1; ++x < ids.size();) {
                    s.setInt(1, trustSettings.getPlayerId());
                    s.setInt(2, ids.get(x));
                    s.setString(3, trustSettings.getTrustLevel(ids.get(x)).name());
                    s.addBatch();
                }
            }

            s.executeBatch();
        }
    }

    private void removeDirtyTrustSettings(Map<ClaimTrustSettings, List<Integer>> toUpdate) throws SQLException {
        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("DELETE FROM " + trustTable + " WHERE id = ? AND targetId = ?")) {
            for (Map.Entry<ClaimTrustSettings, List<Integer>> entry : toUpdate.entrySet()) {
                ClaimTrustSettings trustSettings = entry.getKey();
                List<Integer> ids = entry.getValue();

                for (int x = -1; ++x < ids.size();) {
                    s.setInt(1, trustSettings.getPlayerId());
                    s.setInt(2, ids.get(x));
                    s.addBatch();
                }
            }

            s.executeBatch();
        }
    }

}
