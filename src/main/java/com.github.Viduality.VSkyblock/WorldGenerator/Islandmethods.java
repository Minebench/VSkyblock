package com.github.Viduality.VSkyblock.WorldGenerator;

import com.github.Viduality.VSkyblock.Commands.Island;
import com.github.Viduality.VSkyblock.Listener.CobblestoneGenerator;
import com.github.Viduality.VSkyblock.Utilitys.*;
import com.github.Viduality.VSkyblock.VSkyblock;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Islandmethods {

    private VSkyblock plugin = VSkyblock.getInstance();
    private DatabaseWriter databaseWriter = new DatabaseWriter();
    private DatabaseReader databaseReader = new DatabaseReader();
    private WorldManager wm = new WorldManager();

    /**
     * Checks if a string is from type Integer
     * @param s
     * @return boolean
     */
    private boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Copies the template island and creates a new world for the player.
     * @param uuid
     * @param oldIsland
     */
    public void createNewIsland(UUID uuid, String oldIsland) {
        Player player = plugin.getServer().getPlayer(uuid);
        String worldsizeString = plugin.getConfig().getString("WorldSize");
        String difficulty = plugin.getConfig().getString("Difficulty");
        databaseReader.getLatestIsland(new DatabaseReader.CallbackStrings() {
            @Override
            public void onQueryDone(String result, boolean a) {
                if (a) {
                    wm.createIsland(result);


                    /*
                     * World Size
                     */

                    plugin.getServer().getWorld(result).getWorldBorder().setCenter(0, 0);

                    if (worldsizeString != null) {
                        if (isInt(worldsizeString)) {
                            Integer worldsize = Integer.valueOf(worldsizeString);
                            if (worldsize <= 2000) {
                                plugin.getServer().getWorld(result).getWorldBorder().setSize(worldsize);
                            }
                        }
                    } else {
                        plugin.getServer().getWorld(result).getWorldBorder().setSize(500);
                    }


                    /*
                     * Difficulty
                     */

                    String finaldifficutly = difficulty;

                    if (difficulty != null) {
                        if (difficulty.equalsIgnoreCase("EASY")) {
                            plugin.getServer().getWorld(result).setDifficulty(Difficulty.EASY);
                        } else if (difficulty.equalsIgnoreCase("HARD")) {
                            plugin.getServer().getWorld(result).setDifficulty(Difficulty.HARD);
                        } else if (difficulty.equalsIgnoreCase("PEACEFUL")) {
                            plugin.getServer().getWorld(result).setDifficulty(Difficulty.PEACEFUL);
                        } else {
                            plugin.getServer().getWorld(result).setDifficulty(Difficulty.NORMAL);
                            finaldifficutly = "NORMAL";
                        }
                    } else {
                        plugin.getServer().getWorld(result).setDifficulty(Difficulty.NORMAL);
                        finaldifficutly = "NORMAL";
                    }

                    plugin.getServer().getWorld(result).setGameRule(GameRule.DO_INSOMNIA, false);


                    Island.restartmap.asMap().remove(player.getUniqueId());
                    Island.playerislands.put(uuid, result);
                    CobblestoneGenerator.islandGenLevel.put(result, 0);
                    CobblestoneGenerator.islandlevels.put(result, 0);
                    plugin.getServer().getPlayer(player.getUniqueId()).teleport(plugin.getServer().getWorld(result).getSpawnLocation());
                    if (oldIsland != null) {
                        wm.unloadWorld(oldIsland);
                    }
                    databaseWriter.addIsland(result, uuid, finaldifficutly.toUpperCase());
                    databaseWriter.updateDeathCount(uuid, 0);
                    plugin.scoreboardmanager.updatePlayerScore(player.getName(), "deaths", 0);
                } else {
                    ConfigShorts.messagefromString("FailedToCreateIsland", player);
                }
            }
        });
    }
}

