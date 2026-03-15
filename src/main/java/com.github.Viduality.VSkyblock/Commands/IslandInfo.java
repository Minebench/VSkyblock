package com.github.Viduality.VSkyblock.Commands;

import com.github.Viduality.VSkyblock.Challenges.ChallengesManager;
import com.github.Viduality.VSkyblock.Utilitys.ChallengesCache;
import com.github.Viduality.VSkyblock.Utilitys.ConfigShorts;
import com.github.Viduality.VSkyblock.Utilitys.IslandCacheHandler;
import com.github.Viduality.VSkyblock.Utilitys.PlayerInfo;
import com.github.Viduality.VSkyblock.VSkyblock;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.Duration;

public class IslandInfo extends PlayerSubCommand {

    public IslandInfo(VSkyblock plugin) {
        super(plugin, "info");
    }

    @Override
    public void execute(CommandSender sender, PlayerInfo playerInfo, String[] args) {
        if (playerInfo.getIslandId() == 0) {
            ConfigShorts.messagefromString("NoIsland", playerInfo.getPlayer());
            return;
        }

        Player player = playerInfo.getPlayer();
        if (player == null) {
            return;
        }

        plugin.getDb().getReader().getIslandChallenges(playerInfo.getIslandId(), (ChallengesCache cache) -> {
            plugin.getDb().getReader().getIslandLevelFromUuid(player.getUniqueId(), islandLevel -> {
                plugin.getDb().getReader().getIslandMembers(playerInfo.getIslandId(), members -> {
                    int completedChallenges = (int) cache.getAllChallengeCounts().values().stream().filter(v -> v != null && v > 0).count();
                    int totalChallenges = ChallengesManager.challenges.size();

                    String islandName = playerInfo.getIslandName() != null ? playerInfo.getIslandName() : "unknown";
                    World world = plugin.getServer().getWorld(islandName);

                    String ageText = "unknown";
                    String buildRangeText = "unknown";
                    if (world != null) {
                        File worldFolder = world.getWorldFolder();
                        if (worldFolder != null && worldFolder.exists()) {
                            long ageMillis = Math.max(0, System.currentTimeMillis() - worldFolder.lastModified());
                            long days = Duration.ofMillis(ageMillis).toDays();
                            long hours = Duration.ofMillis(ageMillis).toHours() % 24;
                            ageText = days + "d " + hours + "h";
                        }

                        double radius = world.getWorldBorder().getSize() / 2.0;
                        buildRangeText = "±" + String.format("%.0f", radius) + " blocks (X/Z)";
                    } else if (ConfigShorts.getDefConfig().contains("WorldSize")) {
                        double radius = ConfigShorts.getDefConfig().getDouble("WorldSize", 500D) / 2.0;
                        buildRangeText = "±" + String.format("%.0f", radius) + " blocks (X/Z)";
                    }

                    sender.sendMessage(ChatColor.AQUA + "--- Island Info ---");
                    sender.sendMessage(ChatColor.GOLD + "Island: " + ChatColor.RESET + islandName);
                    sender.sendMessage(ChatColor.GOLD + "Age: " + ChatColor.RESET + ageText);
                    sender.sendMessage(ChatColor.GOLD + "Build range: " + ChatColor.RESET + buildRangeText);
                    sender.sendMessage(ChatColor.GOLD + "Level: " + ChatColor.RESET + islandLevel);
                    sender.sendMessage(ChatColor.GOLD + "Members: " + ChatColor.RESET + members.size());
                    sender.sendMessage(ChatColor.GOLD + "Challenge progress: " + ChatColor.RESET
                            + completedChallenges + "/" + totalChallenges);

                    Integer cachedLevel = IslandCacheHandler.islandlevels.get(islandName);
                    if (cachedLevel == null || !cachedLevel.equals(islandLevel)) {
                        IslandCacheHandler.islandlevels.put(islandName, islandLevel);
                    }
                });
            });
        });
    }
}
