package com.github.Viduality.VSkyblock.Commands;

import com.github.Viduality.VSkyblock.SQLConnector;
import com.github.Viduality.VSkyblock.Utilitys.ConfigShorts;
import com.github.Viduality.VSkyblock.Utilitys.DatabaseCache;
import com.github.Viduality.VSkyblock.Utilitys.DatabaseWriter;
import com.github.Viduality.VSkyblock.Utilitys.WorldManager;
import com.github.Viduality.VSkyblock.VSkyblock;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class IslandKick implements SubCommand{

    private VSkyblock plugin = VSkyblock.getInstance();
    private SQLConnector getDatabase = new SQLConnector();
    private DatabaseWriter databaseWriter = new DatabaseWriter();
    private WorldManager wm = new WorldManager();


    @Override
    public void execute(DatabaseCache databaseCache) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Player player = databaseCache.getPlayer();
                OfflinePlayer target = plugin.getServer().getOfflinePlayer(databaseCache.getArg());
                if (target != null) {
                    if (target != player) {
                        UUID targetuuid = target.getUniqueId();
                        Set<UUID> members = new LinkedHashSet<>();
                        Connection connection = getDatabase.getConnection();
                        try {
                            PreparedStatement preparedStatement;
                            preparedStatement = connection.prepareStatement("SELECT * FROM VSkyblock_Player WHERE islandid = ?");
                            preparedStatement.setInt(1, databaseCache.getIslandId());
                            ResultSet resultSet = preparedStatement.executeQuery();
                            while (resultSet.next()) {
                                members.add(UUID.fromString(resultSet.getString("uuid")));
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            getDatabase.closeConnection(connection);
                        }
                        if (members.contains(targetuuid)) {
                            if (databaseCache.isIslandowner()) {
                                databaseWriter.kickPlayerfromIsland(targetuuid);
                                databaseWriter.resetChallenges(targetuuid);
                                ConfigShorts.custommessagefromString("KickedMember", player, player.getName(), target.getName());
                                if (target.isOnline()) {
                                    Player onlinetarget = (Player) target;
                                    ConfigShorts.messagefromString("KickedFromIsland", onlinetarget);
                                    onlinetarget.getInventory().clear();
                                    onlinetarget.getEnderChest().clear();
                                    databaseWriter.removeKicked(targetuuid);
                                    Island.playerislands.remove(targetuuid);
                                }
                            } else {
                                ConfigShorts.messagefromString("NotIslandOwner", player);
                            }
                        } else {
                            if (target.isOnline()) {
                                Player onlinetarget = (Player) target;
                                if (!onlinetarget.hasPermission("VSkyblock.IgnoreKick")) {
                                    if (onlinetarget.getWorld().getName().equals(databaseCache.getIslandname())) {
                                        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                                            @Override
                                            public void run() {
                                                onlinetarget.teleport(wm.getSpawnLocation(plugin.getConfig().getString("SpawnWorld")));
                                                ConfigShorts.messagefromString("KickVisitingPlayer", onlinetarget);
                                            }
                                        });
                                    } else {
                                        ConfigShorts.messagefromString("PlayerNotIslandMember", player);
                                    }
                                } else {
                                    ConfigShorts.messagefromString("PlayerNotIslandMember", player);
                                }
                            } else {
                                ConfigShorts.messagefromString("PlayerNotIslandMember", player);
                            }
                        }
                    } else {
                        ConfigShorts.messagefromString("CantKickYourself", player);
                    }
                } else {
                    ConfigShorts.messagefromString("PlayerDoesNotExist", player);
                }
            }
        });
    }
}
