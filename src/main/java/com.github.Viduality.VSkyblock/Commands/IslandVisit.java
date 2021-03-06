package com.github.Viduality.VSkyblock.Commands;

import com.github.Viduality.VSkyblock.Utilitys.ConfigShorts;
import com.github.Viduality.VSkyblock.Utilitys.DatabaseCache;
import com.github.Viduality.VSkyblock.Utilitys.DatabaseReader;
import com.github.Viduality.VSkyblock.Utilitys.WorldManager;
import com.github.Viduality.VSkyblock.VSkyblock;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class IslandVisit implements SubCommand {

    private VSkyblock plugin = VSkyblock.getInstance();
    private DatabaseReader databaseReader = new DatabaseReader();
    private WorldManager wm = new WorldManager();


    @Override
    public void execute(DatabaseCache databaseCache) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Player player = databaseCache.getPlayer();
                OfflinePlayer target = plugin.getServer().getOfflinePlayer(databaseCache.getArg());
                if (player != target) {
                    if (target.isOnline()) {
                        Player onlinetarget = plugin.getServer().getPlayer(databaseCache.getArg());
                        UUID uuid = onlinetarget.getUniqueId();
                        databaseReader.getislandidfromplayer(uuid, new DatabaseReader.CallbackINT() {
                            @Override
                            public void onQueryDone(int result) {
                                int islandid = result;
                                databaseReader.getIslandMembers(result, new DatabaseReader.CallbackList() {
                                    @Override
                                    public void onQueryDone(List<String> result) {
                                        if (!result.contains(player.getName())) {
                                            databaseReader.isislandvisitable(islandid, new DatabaseReader.CallbackBoolean() {
                                                @Override
                                                public void onQueryDone(boolean result) {
                                                    if (result) {
                                                        databaseReader.getislandnamefromplayer(uuid, new DatabaseReader.CallbackString() {
                                                            @Override
                                                            public void onQueryDone(String result) {
                                                                if (wm.getLoadedWorlds().contains(result)) {
                                                                    if (wm.getSpawnLocation(result).getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
                                                                        if (wm.getSpawnLocation(result).getBlock().getType().equals(Material.AIR)) {
                                                                            if (wm.getSpawnLocation(result).getBlock().getRelative(BlockFace.UP).getType().equals(Material.AIR)) {
                                                                                player.teleport(wm.getSpawnLocation(result));
                                                                                databaseReader.getIslandMembers(islandid, new DatabaseReader.CallbackList() {
                                                                                    @Override
                                                                                    public void onQueryDone(List<String> result) {
                                                                                        for (String member : result) {
                                                                                            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(member);
                                                                                            if (offlinePlayer.isOnline()) {
                                                                                                Player onlinePlayer = (Player) offlinePlayer;
                                                                                                ConfigShorts.custommessagefromString("PlayerVisitingYourIsland", onlinePlayer, player.getName());
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                });
                                                                            } else {
                                                                                ConfigShorts.messagefromString("IslandSpawnNotSafe", player);
                                                                            }
                                                                        } else {
                                                                            ConfigShorts.messagefromString("IslandSpawnNotSafe", player);
                                                                        }
                                                                    } else {
                                                                        ConfigShorts.messagefromString("IslandSpawnNotSafe", player);
                                                                    }
                                                                } else {
                                                                    ConfigShorts.messagefromString("IslandSpawnNotSafe", player);
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        ConfigShorts.messagefromString("CannotVisitIsland", player);
                                                    }
                                                }
                                            });
                                        } else {
                                            ConfigShorts.messagefromString("VisitYourself", player);
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        ConfigShorts.custommessagefromString("PlayerNotOnline", player, player.getName(), databaseCache.getArg());
                    }
                } else {
                    ConfigShorts.messagefromString("VisitYourself", player);
                }
            }
        });
    }
}
