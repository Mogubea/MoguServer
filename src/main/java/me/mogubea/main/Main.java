package me.mogubea.main;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.mogubea.claims.Claim;
import me.mogubea.claims.ClaimManager;
import me.mogubea.commands.CommandManager;
import me.mogubea.data.DatasourceCore;
import me.mogubea.entities.CustomEntityManager;
import me.mogubea.guilds.GuildManager;
import me.mogubea.items.MoguItemManager;
import me.mogubea.listeners.ListenerManager;
import me.mogubea.profile.PlayerProfile;
import me.mogubea.profile.PlayerProfileManager;
import me.mogubea.recipes.RecipeManager;
import me.mogubea.statistics.SimpleStatType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Main extends JavaPlugin {

    private static Main INSTANCE; // Augh

    private DatasourceCore datasourceCore;

    private BlockTracker blockTracker;
    private CustomEntityManager customEntityManager;
    private CommandManager commandManager;
    private GuildManager guildManager;
    private PlayerProfileManager profileManager;
    private MoguItemManager itemManager;
    private ClaimManager claimManager;
    private RecipeManager recipeManager;

    private ProtocolManager protocolManager;

    private boolean fullyEnabled;

    @Override
    public void onLoad() {
        customEntityManager = new CustomEntityManager();
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        // Create data folder.
        if (!getDataFolder().exists())
            if (!getDataFolder().mkdir())
                getSLF4JLogger().error("There was a problem creating the plugin Data Folder.");

        Plugin protocolLib = getServer().getPluginManager().getPlugin("ProtocolLib");
        if (protocolLib != null && protocolLib.isEnabled()) {
            protocolManager = ProtocolLibrary.getProtocolManager();
        } else {
            getSLF4JLogger().warn("ProtocolLib was not found! Continuing without it~");
        }

        datasourceCore = new DatasourceCore(this);

        blockTracker = new BlockTracker(this);
        new ListenerManager(this);
        guildManager = new GuildManager(this);
        guildManager.loadGuilds();
        profileManager = new PlayerProfileManager(this);
        profileManager.onEnable(this);
        itemManager = new MoguItemManager(this);
        itemManager.registerItems();
        claimManager = new ClaimManager(this);
        recipeManager = new RecipeManager(this);
        commandManager = new CommandManager(this);

        datasourceCore.doPostCreation();
        startMainLoop();

        fullyEnabled = true;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this); // Disable all events
        getServer().getScheduler().cancelTasks(this); // Cancel all tasks

        // Only bother saving everything if the plugin fully booted properly.
        if (fullyEnabled) {
            profileManager.onDisable();
            commandManager.unregisterCommands();
            recipeManager.unregisterRecipes();
            saveAll();
        }
    }

    /**
     * Save everything
     */
    public void saveAll() {
        datasourceCore.saveAll();
        blockTracker.save();
    }

    int timeUpdateLoop = 4;
    private void startMainLoop() {
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            boolean isNewDay = profileManager.checkForDayChange();
            if (isNewDay) getServer().broadcast(Component.text("New day, new stats~"));

            getServer().getOnlinePlayers().forEach((player) -> {
                PlayerProfile profile = PlayerProfile.from(player);

                // New Day
                if (isNewDay)
                    profile.getStats().clearDailyStats();

                // Update current claim
                Claim claimAtLocation = getClaimManager().getClaim(player.getChunk());
                if (profile.getCurrentClaim() != claimAtLocation) {
                    if (claimAtLocation == null) {
                        player.sendActionBar(Component.text("Wilderness", NamedTextColor.DARK_GREEN));
                        visualiseChunk(player, profile.getCurrentClaim().getChunk(), Particle.SOUL_FIRE_FLAME);
                    } else if (profile.getCurrentClaim() == null || profile.getCurrentClaim().getOwnerId() != claimAtLocation.getOwnerId()) {
                        player.sendActionBar(Component.text(claimAtLocation.getPlayerProfile().getDisplayName() + "'s Claim", NamedTextColor.AQUA));
                        visualiseChunk(player, claimAtLocation.getChunk(), Particle.SOUL_FIRE_FLAME);
                    }

                    profile.setCurrentClaim(claimAtLocation);
                }

                // Update sidebar and stats
                profile.getStats().addToStat(SimpleStatType.GENERIC, "playtime", 1);
                profile.getSidebar().updatePlaytime();

                if (timeUpdateLoop >= 4)
                    profile.getSidebar().updateTime();

                // Update current gui
                if (profile.getMoguGui() != null)
                    profile.getMoguGui().onTick();
            });

            if (++timeUpdateLoop >= 5)
                timeUpdateLoop = 0;
        }, 20L, 20L).getTaskId();
    }

    /**
     * Get an online player whether it's by nickname or username.
     */
    public @Nullable Player searchForPlayer(String name) {
        final Collection<? extends Player> online = getServer().getOnlinePlayers();
        List<Player> targets = new ArrayList<>(online);
        int size = targets.size();

        for (int x = -1; ++x < size;) {
            Player p = targets.get(x);
            PlayerProfile pp = PlayerProfile.from(p);
            if (pp.getDisplayName().equalsIgnoreCase(name) || pp.getName().equalsIgnoreCase(name))
                return p;
        }

        String lowerName = name.toLowerCase();
        if (lowerName.length() >= 2) {
            for (int x = -1; ++x < size;) {
                Player p = targets.get(x);
                PlayerProfile pp = PlayerProfile.from(p);
                if (pp.getDisplayName().toLowerCase().contains(lowerName) || pp.getName().toLowerCase().contains(lowerName))
                    return p;
            }
        }
        return null;
    }

    // TODO: Move this method
    public void visualiseChunk(Player player, Chunk chunk, Particle particle) {
        for (int x = -1; ++x < 17;)
            for (int z = -1; ++z < 17;) {
                if (x != 0 && x != 16 && z != 0 && z != 16) continue;
                int y = player.getLocation().getBlockY();
                player.spawnParticle(particle, (chunk.getX()*16) + x, y, (chunk.getZ()*16) + z, 0, 0, 1, 0, 0.5);
                player.spawnParticle(particle, (chunk.getX()*16) + x, y, (chunk.getZ()*16) + z, 0, 0, -1, 0, 0.5);
            }
    }

    public @NotNull BlockTracker getBlockTracker() {
        return blockTracker;
    }

    public @NotNull CustomEntityManager getCustomEntityManager() {
        return customEntityManager;
    }

    public @NotNull DatasourceCore getDatasourceCore() {
        return datasourceCore;
    }

    public @NotNull GuildManager getGuildManager() {
        return guildManager;
    }

    public @NotNull PlayerProfileManager getProfileManager() {
        return profileManager;
    }

    public @NotNull CommandManager getCommandManager() {
        return commandManager;
    }

    public @NotNull MoguItemManager getItemManager() {
        return itemManager;
    }

    public @NotNull ClaimManager getClaimManager() {
        return claimManager;
    }

    public boolean hasProtocolManager() {
        return protocolManager != null;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public static @NotNull Main getInstance() {
        return INSTANCE;
    }

}