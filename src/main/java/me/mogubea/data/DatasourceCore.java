package me.mogubea.data;

import me.mogubea.main.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class DatasourceCore {

    private final DynmapAPI dynmap;
    private final Set<PrivateDatasource> registeredSources = new HashSet<>();
//    private final Set<PrivateLogger<?>> registeredLoggers = new HashSet<>();

    private final Main plugin;
    private final String host, database, username, password;
    private final int port;

    private Connection connection;

    public DatasourceCore(Main plugin) {
        this.plugin = plugin;
        DynmapAPI dynmapAPI = null;
        FileConfiguration datasourceConfig = getDatasourceConfig();

        host = datasourceConfig.getString("host");
        port = datasourceConfig.getInt("port");
        username = datasourceConfig.getString("username");
        database = datasourceConfig.getString("database");
        password = datasourceConfig.getString("password");

        Plugin pluginDynmap = plugin.getServer().getPluginManager().getPlugin("dynmap");
        if (pluginDynmap != null && pluginDynmap.isEnabled()) {
            dynmapAPI = (DynmapAPI) pluginDynmap;
            if (!dynmapAPI.markerAPIInitialized())
                dynmapAPI = null;
        }

        if ((dynmap = dynmapAPI) == null)
            plugin.getSLF4JLogger().warn("Dynmap API was not found, continuing without it...");

        try {
            synchronized (plugin) {
                if (connection != null && !connection.isClosed()) return;

                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = getNewConnection();
                plugin.getSLF4JLogger().info("Successfully established an MySQL Connection!");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Establish a new MySQL {@link Connection}.
     * @return the new {@link Connection}.
     */
    public Connection getNewConnection() throws SQLException {
        try {
            if (connection != null)
                connection.close();
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&character_set_server=utf8mb4", username, password);
        } catch (SQLException e) {
            getPlugin().getSLF4JLogger().error("Could not establish a new MySQL Connection instance.");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * @return the current {@link Connection}.
     */
    public Connection getConnection() {
        return connection;
    }

    public void close(Object... c) {
        try {
            for (int i = 0; c != null && i < c.length; i++) {
                if (c[i] instanceof ResultSet && !((ResultSet) c[i]).isClosed()) {
                    ((ResultSet) c[i]).close();
                }
            }
            for (int i = 0; c != null && i < c.length; i++) {
                if (c[i] instanceof Statement && !((Statement) c[i]).isClosed()) {
                    ((Statement) c[i]).close();
                }
            }
            for (int i = 0; c != null && i < c.length; i++) {
                if (c[i] instanceof Connection && !((Connection) c[i]).isClosed()) {
                    ((Connection) c[i]).close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public @NotNull Main getPlugin() {
        return plugin;
    }

    /**
     * @return is dynmapAPI enabled?
     */
    public boolean isDynmapEnabled() {
        return dynmap != null;
    }

    /**
     * Please check {@link #isDynmapEnabled()} before using this.
     * @return the dynmapAPI.
     */
    public DynmapAPI getDynmapAPI() {
        return dynmap;
    }

    protected void registerDatasource(PrivateDatasource source) {
        this.registeredSources.add(source);
    }

    public void doPostCreation() {
        registeredSources.forEach(PrivateDatasource::postCreation);
    }

    protected YamlConfiguration getDatasourceConfig() {
        File trackingFile = new File(plugin.getDataFolder() + "/DatasourceConfig.yml");
        if (!trackingFile.exists()) {
            try {
                if (trackingFile.createNewFile())
                    plugin.getSLF4JLogger().info("Created DatasourceConfig.yml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(trackingFile);
    }


//    protected void registerLogger(PrivateLogger<?> logger) {
//        this.registeredLoggers.add(logger);
//    }

    /**
     * Save everything.
     */
    public void saveAll() {
        registeredSources.forEach(datasource -> {
            try {
                datasource.saveAll();
            } catch (Exception e) {
                plugin.getSLF4JLogger().error("There was a problem with saving " + datasource.getClass().getPackageName());
                e.printStackTrace();
            }
        });

//        saveLogs();
    }

//    public void saveLogs() {
//        registeredLoggers.forEach(logger -> {
//            try {
//                logger.saveLogs();
//            } catch (Exception e) {
//                plugin.getSLF4JLogger().error("There was a problem with saving " + logger.getClass().getPackageName() + "'s logs.");
//            }
//        });
//    }

}
