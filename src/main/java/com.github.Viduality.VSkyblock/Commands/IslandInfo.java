package com.github.Viduality.VSkyblock.Commands;

import com.github.Viduality.VSkyblock.Challenges.ChallengesManager;
import com.github.Viduality.VSkyblock.Utilitys.ConfigShorts;
import com.github.Viduality.VSkyblock.Utilitys.PlayerInfo;
import com.github.Viduality.VSkyblock.VSkyblock;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IslandInfo extends PlayerSubCommand {

    public IslandInfo(VSkyblock plugin) {
        super(plugin, "info", "i");
    }

    @Override
    public void execute(CommandSender sender, PlayerInfo playerInfo, String[] args) {
        Player player = playerInfo.getPlayer();
        if (playerInfo.getIslandId() == 0) {
            ConfigShorts.messagefromString("NoIsland", player);
            return;
        }

        if (args.length > 0) {
            ConfigShorts.messagefromString("FalseInput", sender);
            return;
        }

        World world = plugin.getServer().getWorld(playerInfo.getIslandName());
        int islandLevel = playerInfo.getIslandLevel();
        double worldBorderSize = world != null ? world.getWorldBorder().getSize() : 0;
        long worldAgeDays = world != null ? world.getFullTime() / 24000L : 0;

        plugin.getDb().getReader().getIslandChallenges(playerInfo.getIslandId(), challengeCache -> {
            plugin.getDb().getReader().getIslandsChallengePoints(playerInfo.getIslandId(), challengePoints -> {
                int completedChallenges = challengeCache.getAllChallengeCounts().size();
                int totalChallenges = ChallengesManager.challenges.size();

                player.sendMessage(ChatColor.GOLD + "--- Island Info ---");
                player.sendMessage(ChatColor.YELLOW + "Island: " + ChatColor.WHITE + playerInfo.getIslandName());
                player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + islandLevel);
                player.sendMessage(ChatColor.YELLOW + "Build size (X/Z): " + ChatColor.WHITE + (int) worldBorderSize + " x " + (int) worldBorderSize);
                player.sendMessage(ChatColor.YELLOW + "Island age (loaded world days): " + ChatColor.WHITE + worldAgeDays + "d");
                player.sendMessage(ChatColor.YELLOW + "Challenge progress: " + ChatColor.WHITE + completedChallenges + "/" + totalChallenges);
                player.sendMessage(ChatColor.YELLOW + "Challenge points: " + ChatColor.WHITE + challengePoints);
            });
        });
    }
}
