package me.mogubea.profile;

import me.mogubea.data.PrivateDatasource;
import me.mogubea.guilds.PlayerGuildData;
import me.mogubea.main.Main;
import me.mogubea.statistics.DirtyVal;
import me.mogubea.statistics.PlayerStatistics;
import me.mogubea.statistics.SimpleStatType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;

public class PlayerProfileDatasource extends PrivateDatasource {

    private final PlayerProfileManager manager;
    private final String profileTable = getDatasourceConfig().getString("tables.profiles");
    private final String statsTable = getDatasourceConfig().getString("tables.playerstats");
    private final String dailyStatsTable = getDatasourceConfig().getString("tables.dailyplayerstats");
    private final String relationsTable = getDatasourceConfig().getString("tables.playerrelations");
    private final String teleportRelationsTable = getDatasourceConfig().getString("tables.playerteleportrelations");

    private final HashMap<Integer, UUID> idToUUID;
    private boolean isSavingProfiles = false;

    protected PlayerProfileDatasource(Main plugin, PlayerProfileManager manager) {
        super(plugin);
        this.manager = manager;
        idToUUID = loadIdToUUIDMap();
    }

    @Override
    public void saveAll() {
        isSavingProfiles = true;

        Collection<PlayerProfile> profiles = manager.getCache().asMap().values();
        for (PlayerProfile pp : profiles) {
            try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("UPDATE " + profileTable + " SET " +
                    "name = ?, nickname = ?, nameColour = ?, nameGradient = ? WHERE id = ?")) {
                int idx = 0;

                s.setString(++idx, pp.getName());
                s.setString(++idx, pp.hasNickname() ? pp.getNickname() : null);
                s.setInt(++idx, pp.hasCustomNameColour() ? pp.getNameColour().value() : 0);
                s.setInt(++idx, pp.getNameGradient());
                s.setInt(++idx, pp.getId());
                s.executeUpdate();
            } catch (SQLException e) {
                getPlugin().getSLF4JLogger().error("There was a problem saving " + pp.getName() + "'s PlayerProfile;");
                e.printStackTrace();
            }

            savePlayerStats(pp);
        }

        isSavingProfiles = false;
    }

    protected boolean isCurrentlySaving() {
        return isSavingProfiles;
    }

    private HashMap<Integer, UUID> loadIdToUUIDMap() {
        HashMap<Integer, UUID> map = new HashMap<>();
        // TODO: replace this with an offlineprofile type system
        ResultSet rs = null;

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT id,uuid FROM " + profileTable)) {
            rs = s.executeQuery();

            while(rs.next())
                map.put(rs.getInt("id"), UUID.fromString(rs.getString("uuid")));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
        }

        return map;
    }

    protected @Nullable UUID getUUIDfromId(int id) {
        return idToUUID.get(id);
    }

    protected @NotNull PlayerProfile getOrCreateProfile(UUID playerUUID) throws Exception {
        return getOrCreateProfile(playerUUID, null);
    }

    protected @NotNull PlayerProfile getOrCreateProfile(UUID playerUUID, @Nullable String username) throws Exception {
        PlayerProfile playerProfile = getProfile(playerUUID);
        if (playerProfile != null) return playerProfile;
        ResultSet rs = null;

        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + profileTable + " (uuid, name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, playerUUID.toString());
            s.setString(2, username);
            s.executeUpdate();

            rs = s.getGeneratedKeys();

            if (rs.next()) {
                int id = rs.getInt(1);
                getPlugin().getSLF4JLogger().info("Player Profile has been created for " + username + " (#" + id + ").");
                return new PlayerProfile(manager, id, playerUUID, null);
            }
        } catch (SQLException e) {
            throw new Exception("There was a problem creating a profile for " + playerUUID, e);
        } finally {
            close(rs);
        }

        throw new Exception("There was an unknown problem creating a profile for " + playerUUID);
    }

    protected @Nullable PlayerProfile getProfile(@NotNull UUID playerUUID) {
        ResultSet rs = null;

        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM " + profileTable + " WHERE uuid = ?")) {
            s.setString(1, playerUUID.toString());
            rs = s.executeQuery();
            if (rs.next())
                return new PlayerProfile(manager, rs.getInt("id"), playerUUID, rs.getString("nickname"));
        } catch (SQLException e) {
            return null;
        } finally {
            close(rs);
        }
        return null;
    }

    /**
     * Load the player's {@link PlayerRelations}.
     */
    protected @NotNull PlayerRelations loadPlayerRelations(@NotNull final PlayerProfile profile) {
        HashMap<Integer, RelationInfo> friends = new HashMap<>();
        HashMap<Integer, RelationInfo> blocked = new HashMap<>();
        Set<Integer> tpAllow = new HashSet<>();
        Set<Integer> tpDeny = new HashSet<>();

        ResultSet rs = null;

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM " + relationsTable + " WHERE id = ? OR (targetId = ? AND positive = 1)")) {
            s.setInt(1, profile.getId());
            s.setInt(2, profile.getId());
            rs = s.executeQuery();

            while(rs.next()) {
                if (rs.getBoolean("positive"))
                    friends.put(rs.getInt("targetId"), new RelationInfo(rs.getTimestamp("relationTime")));
                else
                    blocked.put(rs.getInt("targetId"), new RelationInfo(rs.getTimestamp("relationTime")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
        }

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM " + teleportRelationsTable + " WHERE id = ?")) {
            s.setInt(1, profile.getId());
            rs = s.executeQuery();

            while(rs.next()) {
                if (rs.getBoolean("allow"))
                    tpAllow.add(rs.getInt("targetId"));
                else
                    tpDeny.add(rs.getInt("targetId"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
        }

        return new PlayerRelations(profile, friends, blocked, tpAllow, tpDeny);
    }

    protected boolean editPlayerRelation(@NotNull final PlayerProfile profile, int targetId, boolean clear, boolean friendly) {
        String statement = "INSERT INTO " + relationsTable + " (id, targetId, positive, relationTime) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE positive = VALUES(positive), relationTime = VALUES(relationTime)";
        if (clear) statement = "DELETE FROM " + relationsTable + "WHERE (id = ? AND targetId = ?) OR (targetId = ? AND id = ?)";

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement(statement)) {
            int idx = 0;
            s.setInt(++idx, profile.getId());
            s.setInt(++idx, targetId);
            if (clear) {
                s.setInt(++idx, targetId);
                s.setInt(++idx, profile.getId());
            } else {
                s.setBoolean(++idx, friendly);
                s.setTimestamp(++idx, new Timestamp(System.currentTimeMillis()));
            }

            s.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Save the player's {@link PlayerStatistics}.
     */
    protected void savePlayerStats(PlayerProfile pp) {
        int statDay = manager.getStatDay();
        PlayerStatistics stats = pp.getStats();

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + statsTable + " (id, category, stat, value) VALUES (?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE value = VALUES(value)")) {
            s.setInt(1, pp.getId());

            HashMap<SimpleStatType, HashMap<String, DirtyVal<Integer>>> map = stats.getMap();

            for (Map.Entry<SimpleStatType, HashMap<String, DirtyVal<Integer>>> ent : map.entrySet()) {
                s.setString(2, ent.getKey().getIdentifier());

                for(Map.Entry<String, DirtyVal<Integer>> entry : ent.getValue().entrySet()) {
                    if (!entry.getValue().isDirty()) continue;

                    s.setString(3, entry.getKey());
                    s.setInt(4, entry.getValue().getValue());
                    s.addBatch();
                    entry.getValue().setDirty(false);
                }
            }

            s.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + dailyStatsTable + " (day, id, category, stat, value) VALUES (?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE value = VALUES(value)")) {
            s.setInt(1, statDay);
            s.setInt(2, pp.getId());

            HashMap<SimpleStatType, HashMap<String, DirtyVal<Integer>>> map = stats.getDailyMap();

            for (Map.Entry<SimpleStatType, HashMap<String, DirtyVal<Integer>>> ent : map.entrySet()) {
                s.setString(3, ent.getKey().getIdentifier());

                for(Map.Entry<String, DirtyVal<Integer>> entry : ent.getValue().entrySet()) {
                    if (!entry.getValue().isDirty()) continue;

                    s.setString(4, entry.getKey());
                    s.setInt(5, entry.getValue().getValue());
                    s.addBatch();
                    entry.getValue().setDirty(false);
                }
            }

            s.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the player's {@link PlayerStatistics}.
     */
    protected @NotNull PlayerStatistics loadPlayerStats(@NotNull final PlayerProfile profile) {
        int statDay = manager.getStatDay();
        final PlayerStatistics stats = new PlayerStatistics();
        ResultSet rs = null;

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT category,stat,value FROM " + statsTable + " WHERE id = ?")) {
            s.setInt(1, profile.getId());
            rs = s.executeQuery();

            while(rs.next()) {
                SimpleStatType type = SimpleStatType.fromIdentifier(rs.getString(1));
                if (type != null)
                    stats.setStat(type, rs.getString(2), rs.getInt(3), false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
        }

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT category,stat,value FROM " + dailyStatsTable + " WHERE id = ? AND day = ?")) {
            s.setInt(1, profile.getId());
            s.setInt(2, statDay);
            rs = s.executeQuery();

            while(rs.next()) {
                SimpleStatType type = SimpleStatType.fromIdentifier(rs.getString(1));
                if (type != null)
                    stats.setDailyStat(type, rs.getString(2), rs.getInt(3), false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
        }

        return stats;
    }

    protected @NotNull PlayerGuildData loadGuildData(@NotNull PlayerProfile profile) {
        return plugin.getGuildManager().getDatasource().loadGuildData(profile);
    }

    protected void saveAllPlayerStats() {
        Collection<PlayerProfile> profiles = manager.getCache().asMap().values();
        for (PlayerProfile pp : profiles)
            savePlayerStats(pp);
    }

}
