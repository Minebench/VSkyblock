package com.github.Viduality.VSkyblock.Commands;

import com.github.Viduality.VSkyblock.VSkyblock;
import com.github.Viduality.VSkyblock.Utilitys.ConfigShorts;
import com.github.Viduality.VSkyblock.Utilitys.PlayerInfo;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.Period;

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

        ConfigShorts.messagefromString("IslandInfoLoading", sender);

        plugin.getDb().getReader().getIslandsChallengePoints(playerInfo.getIslandId(), (challengePoints) -> {
            plugin.getDb().getReader().getIslandMembersTotal(playerInfo.getIslandId(), (totalMembers) -> {

                StringBuilder sb = new StringBuilder();

                sb.append(ConfigShorts.getMessageConfig().getString("IslandInfoLabelAge"));
                Timestamp dateCreated = playerInfo.getIslandCreated();
                LocalDateTime createdDateTime = dateCreated.toLocalDateTime();
                LocalDateTime currentDateTime = LocalDateTime.now();

                Period period = Period.between(createdDateTime.toLocalDate(), currentDateTime.toLocalDate());
                Duration duration = Duration.between(createdDateTime, currentDateTime);


                if (period.getYears() > 0) {
                    sb.append(period.getYears())
                            .append(" years, ")
                            .append(period.getMonths())
                            .append(" months, ")
                            .append(period.getDays())
                            .append(" days old");
                } else if (period.getMonths() > 0) {
                    sb.append(" months, ")
                            .append(period.getDays())
                            .append(" days old");
                } else if (period.getDays() > 0) {
                    sb.append(period.getDays())
                            .append(" days old");
                } else if (duration.toHours() > 0) {
                    sb.append(duration.toHours())
                            .append(" hours old");
                } else if (duration.toMinutes() > 0) {
                    sb.append(duration.toMinutes())
                            .append(" minutes old");
                } else {
                    sb.append(duration.toSeconds())
                            .append(" seconds old");
                }
                sb.append("\n");

                World world = plugin.getServer().getWorld(playerInfo.getIslandName());

                double worldsize = world.getWorldBorder().getSize();

                sb.append(ConfigShorts.getMessageConfig().getString("IslandInfoLabelSize"));
                sb.append(worldsize);
                sb.append(" ")
                        .append(ConfigShorts.getMessageConfig().getString("IslandInfoLabelSizeUnits"));
                sb.append("\n");

                sb.append(ConfigShorts.getMessageConfig().getString("IslandInfoLabelDifficulty"));
                sb.append(world.getDifficulty());
                sb.append("\n");

                sb.append(ConfigShorts.getMessageConfig().getString("IslandInfoLabelTotalMembers"));
                sb.append(totalMembers);
                sb.append("\n");

                sb.append(ConfigShorts.getMessageConfig().getString("IslandInfoLabelXBorder"));
                sb.append(worldsize / -2).append(" to ").append(worldsize / 2);
                sb.append("\n");

                sb.append(ConfigShorts.getMessageConfig().getString("IslandInfoLabelZBorder"));
                sb.append(worldsize / -2).append(" to ").append(worldsize / 2);
                sb.append("\n");

                sb.append(ConfigShorts.getMessageConfig().getString("IslandInfoLabelLevel"));
                sb.append(playerInfo.getIslandLevel());
                sb.append("\n");

                sb.append(ConfigShorts.getMessageConfig().getString("IslandInfoLabelProgress"));

                int valueperlevel;
                if (isInt(ConfigShorts.getDefConfig().getString("IslandValue"))) {
                    valueperlevel = ConfigShorts.getDefConfig().getInt("IslandValue");
                } else {
                    valueperlevel = 300;
                }

                double value = 0;
                if (isInt(ConfigShorts.getDefConfig().getString("IslandValueonStart"))) {
                    value = ConfigShorts.getDefConfig().getInt("IslandValueonStart");
                } else {
                    value = 150;
                }
                value = value + challengePoints;

                sb.append(value - (getValueIncrease() * playerInfo.getIslandLevel()))
                        .append("/")
                        .append((getValueIncrease() * playerInfo.getIslandLevel()) + valueperlevel);
                sb.append("\n");

                sb.append(ConfigShorts.getMessageConfig().getString("IslandInfoLabelChallenges"));
                sb.append(playerInfo.getChallengesCompleted());
                sb.append("\n");

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', sb.toString()));
            });
        });
    }

    private static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private static int getValueIncrease() {
        String s = ConfigShorts.getDefConfig().getString("IslandValueIncreasePerLevel");
        if (s != null) {
            if (isInt(s)) {
                return Integer.parseInt(s);
            } else {
                return 20;
            }
        } else {
            return 20;
        }
    }
}