package com.github.Viduality.VSkyblock.Commands.Admin;

/*
 * VSkyblock
 * Copyright (C) 2020  Viduality
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import com.github.Viduality.VSkyblock.Utilitys.IslandCacheHandler;
import com.github.Viduality.VSkyblock.Commands.IslandLevel;
import com.github.Viduality.VSkyblock.Commands.WorldCommands.AdminSubCommand;
import com.github.Viduality.VSkyblock.Utilitys.ConfigShorts;
import com.github.Viduality.VSkyblock.VSkyblock;
import org.bukkit.*;
import org.bukkit.command.CommandSender;

public class RecalculateIslandLevel implements AdminSubCommand {

    private final VSkyblock plugin;

    public RecalculateIslandLevel(VSkyblock plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String args, String option1, String option2) {
        if (sender.hasPermission("VSkyblock.RecalculateIslandLevel")) {
            OfflinePlayer p = plugin.getServer().getOfflinePlayer(args);
            plugin.getDb().getReader().getPlayerData(p.getUniqueId().toString(), (playerdata -> {
                if (playerdata.getUuid() != null) {
                    plugin.getDb().getReader().getIslandIdFromPlayer(playerdata.getUuid(), (islandid -> {
                        if (islandid != 0) {
                            plugin.getDb().getReader().getIslandLevelFromUuid(playerdata.getUuid(), (oldislandlevel -> {
                                ConfigShorts.custommessagefromString("CurrentIslandLevel", sender, String.valueOf(oldislandlevel));
                                plugin.getDb().getReader().getIslandNameFromPlayer(playerdata.getUuid(), (islandname -> {
                                    if (plugin.getWorldManager().loadWorld(islandname)) {
                                        plugin.getDb().getReader().getIslandsChallengePoints(islandid, (challengePoints -> {
                                            World world = plugin.getServer().getWorld(islandname);
                                            if (world == null) {
                                                ConfigShorts.custommessagefromString("WorldNotFound", sender, islandname);
                                                return;
                                            }
                                            int valueriselevel = getValueRiseLevel();
                                            int valueincrease = getValueIncrease();
                                            double value = 0;
                                            if (isInt(ConfigShorts.getDefConfig().getString("IslandValueonStart"))) {
                                                value = ConfigShorts.getDefConfig().getInt("IslandValueonStart");
                                            } else {
                                                value = 150;
                                            }
                                            value = value + challengePoints;
                                            int valueperlevel;
                                            if (isInt(ConfigShorts.getDefConfig().getString("IslandValue"))) {
                                                valueperlevel = ConfigShorts.getDefConfig().getInt("IslandValue");
                                            } else {
                                                valueperlevel = 300;
                                            }

                                            IslandLevel.calculate(world, value, (c) -> {
                                                if (IslandCacheHandler.playerislands.containsValue(islandname)) {
                                                    IslandCacheHandler.islandCounts.put(islandname, c);
                                                }

                                                double currentvalue = c.value;

                                                int level = 0;
                                                int increasedvaluefornextlevel = valueperlevel + valueincrease;
                                                for (int i = 0; i < valueriselevel; i++) {
                                                    if (currentvalue - valueperlevel >= 0) {
                                                        level = level + 1;
                                                        currentvalue = currentvalue - valueperlevel;
                                                    } else {
                                                        currentvalue = 0;
                                                        break;
                                                    }
                                                }
                                                if (currentvalue - increasedvaluefornextlevel >= 0) {
                                                    while (currentvalue >= 0) {
                                                        if (currentvalue - increasedvaluefornextlevel >= 0) {
                                                            level++;
                                                            currentvalue = currentvalue - increasedvaluefornextlevel;
                                                            increasedvaluefornextlevel = increasedvaluefornextlevel + valueincrease;
                                                        } else {
                                                            currentvalue = 0;
                                                            break;
                                                        }
                                                    }
                                                }

                                                int roundlevel = (int) Math.floor(level);
                                                plugin.getDb().getWriter().updateIslandLevel(islandid, roundlevel, c.blocks, c.entities, p.getUniqueId());
                                                ConfigShorts.custommessagefromString("NewIslandLevel", sender, String.valueOf(roundlevel));
                                                if (!IslandCacheHandler.playerislands.containsValue(islandname)) {
                                                    plugin.getWorldManager().unloadWorld(islandname);
                                                }
                                                if (IslandCacheHandler.islandlevels.containsKey(islandname)) {
                                                    IslandCacheHandler.islandlevels.put(islandname, roundlevel);
                                                }
                                            });
                                        }));
                                    } else {
                                        ConfigShorts.custommessagefromString("WorldNotFound", sender, islandname);
                                    }
                                }));
                            }));
                        } else {
                            ConfigShorts.messagefromString("PlayerHasNoIsland", sender);
                        }
                    }));
                } else {
                    ConfigShorts.messagefromString("PlayerDoesNotExist", sender);
                }
            }));
        } else {
            ConfigShorts.messagefromString("PermissionLack", sender);
        }
    }



    private boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private int getValueRiseLevel() {
        String s = ConfigShorts.getDefConfig().getString("IslandValueRiseLevel");
        if (s != null) {
            if (isInt(s)) {
                return Integer.parseInt(s);
            } else {
                return 150;
            }
        } else {
            return 150;
        }
    }

    private int getValueIncrease() {
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
