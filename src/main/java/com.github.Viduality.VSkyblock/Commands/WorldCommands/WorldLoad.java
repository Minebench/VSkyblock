package com.github.Viduality.VSkyblock.Commands.WorldCommands;

import com.github.Viduality.VSkyblock.Utilitys.ConfigShorts;
import com.github.Viduality.VSkyblock.Utilitys.WorldManager;
import org.bukkit.entity.Player;

public class WorldLoad implements AdminSubCommand {

    private WorldManager wm = new WorldManager();

    @Override
    public void execute(Player player, String args, String option1, String option2) {
        if (player.hasPermission("VSkyblock.LoadWorld")) {
            if (wm.getAllWorlds().contains(args)) {
                if (wm.getUnloadedWorlds().contains(args)) {
                    if (wm.loadWorld(args)) {
                        ConfigShorts.messagefromString("WorldLoaded", player);
                    } else {
                        ConfigShorts.custommessagefromString("WorldFailedToLoad", player, args);
                    }
                } else {
                    ConfigShorts.messagefromString("WorldAlreadyLoaded", player);
                }
            } else {
                ConfigShorts.custommessagefromString("NoWorldFound", player, args);
            }
        } else {
            ConfigShorts.messagefromString("PermissionLack", player);
        }
    }
}
