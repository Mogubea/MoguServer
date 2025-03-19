package me.mogubea.guilds;

import me.mogubea.data.PrivateDatasource;
import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuildDatasource extends PrivateDatasource {

    private final GuildManager manager;
    private final String guildDataTable = getDatasourceConfig().getString("tables.playerguilddata");

    protected GuildDatasource(Main plugin, GuildManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @Override
    public void saveAll() {
        List<GuildData> toSave = new ArrayList<>();

        getPlugin().getProfileManager().getLoadedProfiles().forEach(playerProfile -> playerProfile.getGuildData().getDataMap().values().forEach(guildData -> {
            if (guildData.isDirty())
                toSave.add(guildData);
        }));


        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + guildDataTable + " (id,guild,level,levelXp,totalXp,essence,currency,hasStone,amuletSlot) VALUES (?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE level = VALUES(level), totalXp = VALUES(totalXp), levelXp = VALUES(levelXp), essence = VALUES(essence), currency = VALUES(currency), hasStone = VALUES(hasStone), amuletSlot = VALUES(amuletSlot)")) {
            int idx = 0;

            for (int x = -1; ++x < toSave.size();) {
                GuildData data = toSave.get(x);

                s.setInt(++idx, data.getParent().getProfile().getId());
                s.setString(++idx, data.getGuild().getIdentifier());
                s.setInt(++idx, data.getLevel());
                s.setInt(++idx, data.getExperience());
                s.setInt(++idx, data.getTotalExperience());
                s.setInt(++idx, data.getEssence());
                s.setInt(++idx, data.getCurrency());
                s.setBoolean(++idx, data.hasStone());
                s.setByte(++idx, data.getAmuletSlot());

                s.addBatch();
            }

            s.executeBatch();

            // After successful execution, mark all as clean.
            for (int x = -1; ++x < toSave.size();)
                toSave.get(x).setClean();

        } catch (SQLException e) {
            getPlugin().getSLF4JLogger().error("There was an issue with saving player Guild Data!");
            e.printStackTrace();
        }
    }

    public @NotNull PlayerGuildData loadGuildData(@NotNull PlayerProfile profile) {
        PlayerGuildData guildData = new PlayerGuildData(profile);
        Map<Guild, GuildData> guildDataMap = new HashMap<>();
        ResultSet rs = null;
        
        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM " + guildDataTable + " WHERE id = ?")) {
            s.setInt(1, profile.getId());
            rs = s.executeQuery();

            while(rs.next()) {
                Guild guild = manager.getGuild(rs.getString("guild"));
                if (guild == null) continue;

                guildDataMap.put(guild, new GuildData(guildData, guild, rs.getInt("totalXp"), rs.getInt("essence"), rs.getInt("currency"), rs.getBoolean("hasStone"), rs.getByte("amuletSlot")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
        }

        guildData.loadGuildData(guildDataMap);
        return guildData;
    }

}
