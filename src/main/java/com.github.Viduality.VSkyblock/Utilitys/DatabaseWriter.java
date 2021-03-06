package com.github.Viduality.VSkyblock.Utilitys;

import com.github.Viduality.VSkyblock.SQLConnector;
import com.github.Viduality.VSkyblock.VSkyblock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class DatabaseWriter {

    private SQLConnector getDatabase = new SQLConnector();
    private VSkyblock plugin = VSkyblock.getInstance();
    private DatabaseReader databaseReader = new DatabaseReader();
    private WorldManager wm = new WorldManager();

    /**
     * Adds a player to the database. (database action)
     * Is called when the player joins and does not exist in the database.
     * Also inserts the player into the challenges tables.
     * @param uuid
     * @param name
     */
    public void addPlayer(UUID uuid, String name) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection connection = getDatabase.getConnection();
            try {
                PreparedStatement preparedStatement;

                preparedStatement = connection.prepareStatement("INSERT INTO VSkyblock_Player(uuid, playername) VALUES(?, ?)");
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setString(2, name);
                preparedStatement.executeUpdate();
                preparedStatement.close();


                PreparedStatement preparedStatement1;

                preparedStatement1 = connection.prepareStatement("INSERT INTO VSkyblock_Challenges_Easy(uuid) VALUES (?)");
                preparedStatement1.setString(1, uuid.toString());
                preparedStatement1.executeUpdate();
                preparedStatement1 = connection.prepareStatement("INSERT INTO VSkyblock_Challenges_Medium(uuid) VALUES (?)");
                preparedStatement1.setString(1, uuid.toString());
                preparedStatement1.executeUpdate();
                preparedStatement1 = connection.prepareStatement("INSERT INTO VSkyblock_Challenges_Hard(uuid) VALUES (?)");
                preparedStatement1.setString(1, uuid.toString());
                preparedStatement1.executeUpdate();
                preparedStatement1.close();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                getDatabase.closeConnection(connection);
            }
        });
    }

    /**
     * Adds an island into the VSkyblock_Island table and assigns it to an player. (database action)
     * Updates the island AND the player data so the island belongs to the player as its owner.
     * @param island (islandname)
     * @param uuid
     */
    public void addIsland(String island, UUID uuid, String difficutly) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Connection connection = getDatabase.getConnection();
                try {
                    PreparedStatement preparedStatement;
                    preparedStatement = connection.prepareStatement("INSERT INTO VSkyblock_Island(island, difficulty) VALUES(?, ?)");
                    preparedStatement.setString(1, island);
                    preparedStatement.setString(2, difficutly);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                final int[] newislandidarray = {0};
                databaseReader.getislandid(island, new DatabaseReader.CallbackINT() {
                    @Override
                    public void onQueryDone(int result) {
                        newislandidarray[0] = result;
                        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                            @Override
                            public void run() {
                                int islandid = newislandidarray[0];
                                try {
                                    PreparedStatement preparedStatement;
                                    preparedStatement = connection.prepareStatement("UPDATE VSkyblock_Player SET islandid = ?, islandowner = true WHERE uuid = ?");
                                    preparedStatement.setInt(1, islandid);
                                    preparedStatement.setString(2, uuid.toString());
                                    preparedStatement.executeUpdate();
                                    preparedStatement.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                } finally {
                                    getDatabase.closeConnection(connection);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * Removes an player from his island (database action).
     *
     * @param uuid
     */
    public void kickPlayerfromIsland(UUID uuid) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Connection connection = getDatabase.getConnection();
                try {
                    PreparedStatement preparedStatement;
                    preparedStatement = connection.prepareStatement("UPDATE VSkyblock_Player SET islandid = NULL, owneruuid = NULL, kicked = 1 WHERE uuid = ?");
                    preparedStatement.setString(1, uuid.toString());
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    getDatabase.closeConnection(connection);
                }
            }
        });
    }

    /**
     * Removes the assignment "kicked" in the database from a specific player database action).
     *
     * @param uuid
     */
    public void removeKicked(UUID uuid) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection connection = getDatabase.getConnection();
            try {
                PreparedStatement preparedStatement;
                preparedStatement = connection.prepareStatement("UPDATE VSkyblock_Player SET kicked = 0 WHERE uuid = ?");
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                getDatabase.closeConnection(connection);
            }
        });
    }

    /**
     * Updates the owner of an island (database action).
     * Removes the owner title from the old owner and gives it to the new owner.
     * @param oldOwner (uuid)
     * @param newOwner (uuid)
     */
    public void updateOwner(UUID oldOwner, UUID newOwner) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Connection connection = getDatabase.getConnection();
                try {
                    PreparedStatement preparedStatement;
                    preparedStatement = connection.prepareStatement("UPDATE VSkyblock_Player SET islandowner = 0 WHERE uuid = ?");
                    preparedStatement.setString(1, oldOwner.toString());
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    PreparedStatement preparedStatement1;
                    preparedStatement1 = connection.prepareStatement("UPDATE VSkyblock_Player SET islandowner = 1 WHERE uuid = ?");
                    preparedStatement1.setString(1, newOwner.toString());
                    preparedStatement1.executeUpdate();
                    preparedStatement1.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    getDatabase.closeConnection(connection);
                }
            }
        });
    }

    /**
     * Removes an player from his current island (database action).
     *
     * @param uuid
     */
    public void leavefromIsland(UUID uuid) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Connection connection = getDatabase.getConnection();
                try {
                    PreparedStatement preparedStatement;
                    preparedStatement = connection.prepareStatement("UPDATE VSkyblock_Player SET islandid = NULL, owneruuid = NULL, islandowner = 0 WHERE uuid = ?");
                    preparedStatement.setString(1, uuid.toString());
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    getDatabase.closeConnection(connection);
                }
            }
        });
    }

    /**
     * Updates an players island (database action).
     * Overwrites the old island with the new one.
     * @param uuid
     * @param islandid
     * @param islandowner
     */
    public void updatePlayersIsland(UUID uuid, int islandid, boolean islandowner) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection connection = getDatabase.getConnection();
            Integer islandownerInt;
            if (islandowner) {
                islandownerInt = 1;
            } else {
                islandownerInt = 0;
            }
            try {
                PreparedStatement preparedStatement;

                preparedStatement = connection.prepareStatement("UPDATE VSkyblock_Player SET islandid = ?, islandowner = ? WHERE uuid = ?");
                preparedStatement.setInt(1, islandid);
                preparedStatement.setString(2, String.valueOf(islandownerInt));
                preparedStatement.setString(3, uuid.toString());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                getDatabase.closeConnection(connection);
            }
        });
    }

    /**
     * Deletes an island (database action).
     *
     * @param islandname
     */
    public void deleteIsland(String islandname) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection connection = getDatabase.getConnection();
            try {
                PreparedStatement deleteIsland;
                deleteIsland = connection.prepareStatement("DELETE FROM VSkyblock_Island WHERE island = ?");
                deleteIsland.setString(1, islandname);
                deleteIsland.executeUpdate();
                deleteIsland.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                getDatabase.closeConnection(connection);
            }
        });
    }

    /**
     * Updates the count of an challenge (database action).
     *
     * @param uuid
     * @param challengeTable
     * @param challenge (1-18)
     * @param count
     */
    public void updateChallengeCount(UUID uuid, String challengeTable, Integer challenge, int count) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String c = "c" + challenge;
            Connection connection = getDatabase.getConnection();
            try {
                PreparedStatement updateChallengeCount;
                updateChallengeCount = connection.prepareStatement("UPDATE " + challengeTable + " SET " + c + "  = ? WHERE uuid = ?");
                updateChallengeCount.setInt(1, count);
                updateChallengeCount.setString(2, uuid.toString());
                updateChallengeCount.executeUpdate();
                updateChallengeCount.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                getDatabase.closeConnection(connection);
            }
        });
    }

    /**
     * Updates the level of an island (database action).
     *
     * @param islandid
     * @param level
     */
    public void updateIslandLevel(int islandid, Integer level, Integer totalblocks, UUID uuid) {
        databaseReader.getIslandMembers(islandid, new DatabaseReader.CallbackList() {
            @Override
            public void onQueryDone(List<String> result) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    Connection connection = getDatabase.getConnection();
                    try {
                        PreparedStatement updateChallengeCount;
                        updateChallengeCount = connection.prepareStatement("UPDATE VSkyblock_Island SET islandlevel = ?, totalblocks = ? WHERE islandid = ?");
                        updateChallengeCount.setInt(1, level);
                        updateChallengeCount.setInt(2, totalblocks);
                        updateChallengeCount.setInt(3, islandid);
                        updateChallengeCount.executeUpdate();
                        updateChallengeCount.close();

                        PreparedStatement updatehighestreachedlevel;
                        updatehighestreachedlevel = connection.prepareStatement("UPDATE VSkyblock_Player SET highestreachedlevel = ? WHERE uuid = ? AND highestreachedlevel < ?");
                        updatehighestreachedlevel.setInt(1, level);
                        updatehighestreachedlevel.setString(2, uuid.toString());
                        updatehighestreachedlevel.setInt(3, level);
                        updatehighestreachedlevel.executeUpdate();
                        updatehighestreachedlevel.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        getDatabase.closeConnection(connection);
                    }
                });
            }
        });
    }

    /**
     * Resets all challenges of an player (database action).
     * Sets the count for all challenges to 0.
     * @param uuid
     */
    public void resetChallenges(UUID uuid) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Connection connection = getDatabase.getConnection();
                try {
                    PreparedStatement resetEasyChallenges;
                    resetEasyChallenges = connection.prepareStatement("UPDATE VSkyblock_Challenges_Easy SET c1 = 0, c2 = 0, c3 = 0, c4 = 0, c5 = 0, c6 = 0, c7 = 0, c8 = 0, c9 = 0, c10 = 0, c11 = 0, c12 = 0, c13 = 0, c14 = 0, c15 = 0, c16 = 0, c17 = 0, c18 = 0 WHERE uuid = ?");
                    resetEasyChallenges.setString(1, uuid.toString());
                    resetEasyChallenges.executeUpdate();
                    resetEasyChallenges.close();

                    PreparedStatement resetMediumChallenges;
                    resetMediumChallenges = connection.prepareStatement("UPDATE VSkyblock_Challenges_Medium SET c1 = 0, c2 = 0, c3 = 0, c4 = 0, c5 = 0, c6 = 0, c7 = 0, c8 = 0, c9 = 0, c10 = 0, c11 = 0, c12 = 0, c13 = 0, c14 = 0, c15 = 0, c16 = 0, c17 = 0, c18 = 0 WHERE uuid = ?");
                    resetMediumChallenges.setString(1, uuid.toString());
                    resetMediumChallenges.executeUpdate();
                    resetMediumChallenges.close();

                    PreparedStatement resetHardChallenges;
                    resetHardChallenges = connection.prepareStatement("UPDATE VSkyblock_Challenges_Hard SET c1 = 0, c2 = 0, c3 = 0, c4 = 0, c5 = 0, c6 = 0, c7 = 0, c8 = 0, c9 = 0, c10 = 0, c11 = 0, c12 = 0, c13 = 0, c14 = 0, c15 = 0, c16 = 0, c17 = 0, c18 = 0 WHERE uuid = ?");
                    resetHardChallenges.setString(1, uuid.toString());
                    resetHardChallenges.executeUpdate();
                    resetHardChallenges.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    getDatabase.closeConnection(connection);
                }
            }
        });
    }

    /**
     * Writes the given options for the island of the given player into the database.
     * @param player
     * @param visit
     * @param difficulty
     * @param callback
     */
    public void updateIslandOptions(Player player, boolean visit, String difficulty, final Callback callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Connection connection = getDatabase.getConnection();
                try {
                    int islandid = 0;
                    String islandname = null;
                    PreparedStatement preparedStatementREAD;
                    preparedStatementREAD = connection.prepareStatement("SELECT islandid FROM VSkyblock_Player WHERE uuid = ?");
                    preparedStatementREAD.setString(1, player.getUniqueId().toString());
                    ResultSet resultSet = preparedStatementREAD.executeQuery();
                    while (resultSet.next()) {
                        islandid = resultSet.getInt("islandid");
                    }
                    preparedStatementREAD.close();


                    PreparedStatement preparedStatement;
                    preparedStatement = connection.prepareStatement("UPDATE VSkyblock_Island SET visit = ?, difficulty = ? WHERE islandid = ?");
                    preparedStatement.setBoolean(1, visit);
                    preparedStatement.setString(2, difficulty);
                    preparedStatement.setInt(3, islandid);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();

                    PreparedStatement preparedStatementGetIslandName;
                    preparedStatementGetIslandName = connection.prepareStatement("SELECT island FROM VSkyblock_Island WHERE islandid = ?");
                    preparedStatementGetIslandName.setInt(1, islandid);
                    ResultSet resultSet1 = preparedStatementGetIslandName.executeQuery();
                    while (resultSet1.next()) {
                        islandname = resultSet1.getString("island");
                    }
                    preparedStatementGetIslandName.close();

                    wm.setOption(islandname, "difficulty", difficulty);

                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            callback.onQueryDone(true);
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    getDatabase.closeConnection(connection);
                }
            }
        });
    }

    /**
     * Updates the level of an islands cobblestonegenerator (database action).
     *
     * @param islandname
     * @param level
     */
    public void updateCobblestoneGeneratorLevel(String islandname, Integer level) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection connection = getDatabase.getConnection();
            try {
                PreparedStatement updateGeneratorLevel;
                updateGeneratorLevel = connection.prepareStatement("UPDATE VSkyblock_Island SET cobblestonelevel = ? WHERE island = ?");
                updateGeneratorLevel.setInt(1, level);
                updateGeneratorLevel.setString(2, islandname);
                updateGeneratorLevel.executeUpdate();
                updateGeneratorLevel.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                getDatabase.closeConnection(connection);
            }
        });
    }

    /**
     * Updates the death count of a specific player.
     *  @param uuid
     * @param count
     */
    public void updateDeathCount(UUID uuid, int count) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection connection = getDatabase.getConnection();
            try {
                PreparedStatement preparedStatement;
                preparedStatement = connection.prepareStatement("UPDATE VSkyblock_Player SET deaths = ? WHERE uuid = ?");
                preparedStatement.setInt(1, count);
                preparedStatement.setString(2, uuid.toString());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                getDatabase.closeConnection(connection);
            }
        });
    }

    /**
     * Updates the players name after a name change.
     *
     * @param uuid
     * @param name
     */
    public void updatePlayerName(UUID uuid, String name) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection connection = getDatabase.getConnection();
            try {
                PreparedStatement preparedStatement;
                preparedStatement = connection.prepareStatement("UPDATE VSkyblock_Player SET playername = ? WHERE uuid = ?");
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, uuid.toString());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                getDatabase.closeConnection(connection);
            }
        });
    }

    /**
     * Saves the last location of a specific player.
     *
     * @param uuid
     * @param loc
     */
    public void savelastLocation(UUID uuid, Location loc) {
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        double pitch = loc.getPitch();
        double yaw = loc.getYaw();
        String world = loc.getWorld().getName();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Connection connection = getDatabase.getConnection();
                try {
                    PreparedStatement savelastLoc;
                    savelastLoc = connection.prepareStatement("UPDATE VSkyblock_Player SET lastX = ?, lastY = ?, lastZ = ?, lastPitch = ?, lastYaw = ?, lastWorld = ? WHERE uuid = ?");
                    savelastLoc.setDouble(1, x);
                    savelastLoc.setDouble(2, y);
                    savelastLoc.setDouble(3, z);
                    savelastLoc.setDouble(4, pitch);
                    savelastLoc.setDouble(5, yaw);
                    savelastLoc.setString(6, world);
                    savelastLoc.setString(7, uuid.toString());
                    savelastLoc.executeUpdate();
                    savelastLoc.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    getDatabase.closeConnection(connection);
                }
            }
        });
    }

    public interface Callback {
        public void onQueryDone(boolean done);
    }
}
