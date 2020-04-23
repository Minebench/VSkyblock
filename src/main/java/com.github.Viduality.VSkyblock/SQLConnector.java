package com.github.Viduality.VSkyblock;

import com.github.Viduality.VSkyblock.Utilitys.ConfigShorts;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLConnector {

    private static final VSkyblock plugin = VSkyblock.getInstance();
    public static final HikariConfig config = new HikariConfig();
    public static HikariDataSource ds;


    /**
     * Database user username
     * @return Database user username
     */
    public String getDbUser() {
        return ConfigShorts.getDefConfig().getString("database.user");
    }

    /**
     * Database password
     * @return Database password
     */
    public String getDbPassword() {
        return ConfigShorts.getDefConfig().getString("database.password");
    }

    /**
     * Get Database name
     * @return Database name
     */
    public String getDatabase() {
        return ConfigShorts.getDefConfig().getString("database.database");
    }

    /**
     * Get Database URL
     * @return Database URL
     */
    public String getDbUrl() {
        return ConfigShorts.getDefConfig().getString("database.url");
    }

    /**
     * Get Database URL parameters
     * @return Database URL parameters
     */
    public String getDbUrlParameters() {
        return ConfigShorts.getDefConfig().getString("database.url-parameters");
    }

    /**
     * Initiates connection.
     */
    public void initConnection() {
        if (getDatabase() != null && !getDatabase().isEmpty()) {
            initTables();
        }
    }

    {
        config.setJdbcUrl("jdbc:mysql://"
                + getDbUrl() + "/"
                + getDatabase());
        config.setUsername(getDbUser());
        config.setPassword(getDbPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }

    /**
     * Returns the connection to the database.
     * Used to access the database.
     * @return Connection to the database
     */
    public Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Initiate tables
     * Initiates tables and creates them if they don't exist
     */
    public void initTables() {
        try {
            Connection connection = getConnection();
            connection.createStatement().execute(
                    "CREATE DATABASE IF NOT EXISTS " + getDatabase());
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS VSkyblock_Player("
                            + "playername VARCHAR(16) NOT NULL,"
                            + "uuid CHAR(64) UNIQUE NOT NULL,"
                            + "islandid BIGINT,"
                            + "islandowner BOOLEAN NOT NULL DEFAULT false,"
                            + "owneruuid CHAR(32),"
                            + "kicked BOOLEAN NOT NULL DEFAULT false,"
                            + "deaths BIGINT DEFAULT 0,"
                            + "lastX DOUBLE,"
                            + "lastY DOUBLE,"
                            + "lastZ DOUBLE,"
                            + "lastPitch DOUBLE,"
                            + "lastYaw DOUBLE,"
                            + "lastWorld CHAR(128),"
                            + "highestreachedlevel VARCHAR(100) DEFAULT 0,"
                            + "PRIMARY KEY (playername));");
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS VSkyblock_Island("
                            + "islandid BIGINT AUTO_INCREMENT NOT NULL,"
                            + "island VARCHAR(100) NOT NULL,"
                            + "islandlevel VARCHAR(100) DEFAULT 0,"
                            + "difficulty VARCHAR(100) NOT NULL DEFAULT 'NORMAL',"
                            + "visit BOOLEAN NOT NULL DEFAULT TRUE,"
                            + "cobblestonelevel BIGINT DEFAULT 0,"
                            + "totalblocks BIGINT DEFAULT 140,"
                            + "PRIMARY KEY (islandid))");
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS VSkyblock_IslandLocations("
                            + "islandid BIGINT NOT NULL,"
                            + "spawnX DOUBLE NOT NULL DEFAULT 0,"
                            + "spawnY DOUBLE NOT NULL DEFAULT 67,"
                            + "spawnZ DOUBLE NOT NULL DEFAULT 0,"
                            + "spawnYaw FLOAT NOT NULL DEFAULT 0,"
                            + "spawnPitch FLOAT NOT NULL DEFAULT 0,"
                            + "netherX DOUBLE,"
                            + "netherY DOUBLE,"
                            + "netherZ DOUBLE,"
                            + "netherYaw FLOAT,"
                            + "netherWorld CHAR(128),"
                            + "PRIMARY KEY (islandid));");
            connection.createStatement().execute(
                        "CREATE TABLE IF NOT EXISTS VSkyblock_Challenges_Easy("
                            + "uuid CHAR(64) UNIQUE NOT NULL,"
                            + "c1 BIGINT DEFAULT 0,"
                            + "c2 BIGINT DEFAULT 0,"
                            + "c3 BIGINT DEFAULT 0,"
                            + "c4 BIGINT DEFAULT 0,"
                            + "c5 BIGINT DEFAULT 0,"
                            + "c6 BIGINT DEFAULT 0,"
                            + "c7 BIGINT DEFAULT 0,"
                            + "c8 BIGINT DEFAULT 0,"
                            + "c9 BIGINT DEFAULT 0,"
                            + "c10 BIGINT DEFAULT 0,"
                            + "c11 BIGINT DEFAULT 0,"
                            + "c12 BIGINT DEFAULT 0,"
                            + "c13 BIGINT DEFAULT 0,"
                            + "c14 BIGINT DEFAULT 0,"
                            + "c15 BIGINT DEFAULT 0,"
                            + "c16 BIGINT DEFAULT 0,"
                            + "c17 BIGINT DEFAULT 0,"
                            + "c18 BIGINT DEFAULT 0,"
                            + "PRIMARY KEY (uuid));");
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS VSkyblock_Challenges_Medium("
                            + "uuid CHAR(64) UNIQUE NOT NULL,"
                            + "c1 BIGINT DEFAULT 0,"
                            + "c2 BIGINT DEFAULT 0,"
                            + "c3 BIGINT DEFAULT 0,"
                            + "c4 BIGINT DEFAULT 0,"
                            + "c5 BIGINT DEFAULT 0,"
                            + "c6 BIGINT DEFAULT 0,"
                            + "c7 BIGINT DEFAULT 0,"
                            + "c8 BIGINT DEFAULT 0,"
                            + "c9 BIGINT DEFAULT 0,"
                            + "c10 BIGINT DEFAULT 0,"
                            + "c11 BIGINT DEFAULT 0,"
                            + "c12 BIGINT DEFAULT 0,"
                            + "c13 BIGINT DEFAULT 0,"
                            + "c14 BIGINT DEFAULT 0,"
                            + "c15 BIGINT DEFAULT 0,"
                            + "c16 BIGINT DEFAULT 0,"
                            + "c17 BIGINT DEFAULT 0,"
                            + "c18 BIGINT DEFAULT 0,"
                            + "PRIMARY KEY (uuid));");
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS VSkyblock_Challenges_Hard("
                            + "uuid CHAR(64) UNIQUE NOT NULL,"
                            + "c1 BIGINT DEFAULT 0,"
                            + "c2 BIGINT DEFAULT 0,"
                            + "c3 BIGINT DEFAULT 0,"
                            + "c4 BIGINT DEFAULT 0,"
                            + "c5 BIGINT DEFAULT 0,"
                            + "c6 BIGINT DEFAULT 0,"
                            + "c7 BIGINT DEFAULT 0,"
                            + "c8 BIGINT DEFAULT 0,"
                            + "c9 BIGINT DEFAULT 0,"
                            + "c10 BIGINT DEFAULT 0,"
                            + "c11 BIGINT DEFAULT 0,"
                            + "c12 BIGINT DEFAULT 0,"
                            + "c13 BIGINT DEFAULT 0,"
                            + "c14 BIGINT DEFAULT 0,"
                            + "c15 BIGINT DEFAULT 0,"
                            + "c16 BIGINT DEFAULT 0,"
                            + "c17 BIGINT DEFAULT 0,"
                            + "c18 BIGINT DEFAULT 0,"
                            + "PRIMARY KEY (uuid));");
            //Auto adds new columns (if they are implemented in future updates and the plugin is already running on the server)
            connection.createStatement().execute(
                    "ALTER TABLE VSkyblock_Player ADD COLUMN IF NOT EXISTS("
                    + "deaths BIGINT DEFAULT 0,"
                    + "lastX DOUBLE,"
                    + "lastY DOUBLE,"
                    + "lastZ DOUBLE,"
                    + "lastPitch DOUBLE,"
                    + "lastYaw DOUBLE,"
                    + "lastWorld CHAR(128),"
                    + "highestreachedlevel VARCHAR(100) DEFAULT 0);"
            );
            connection.createStatement().execute(
                    "ALTER TABLE VSkyblock_Island ADD COLUMN IF NOT EXISTS("
                    + "totalblocks BIGINT DEFAULT 140);"
            );
            connection.createStatement().execute(
                    "ALTER TABLE VSkyblock_IslandLocations ADD COLUMN IF NOT EXISTS("
                    + "spawnX DOUBLE,"
                    + "spawnY DOUBLE,"
                    + "spawnZ DOUBLE,"
                    + "spawnYaw FLOAT,"
                    + "spawnPitch FLOAT);"
            );
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close connection to the database.
     * @param connection
     */
    public void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            } else {
                VSkyblock.getInstance().getLogger().warning("connection = null, VSkyblock can't close");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Should never be used since it does nothing special ;)
     */
    public void close(){
        ds.close();
    }
}
