package com.github.Viduality.VSkyblock.Listener;

import com.destroystokyo.paper.MaterialTags;
import com.github.Viduality.VSkyblock.Utilitys.IslandCacheHandler;
import com.github.Viduality.VSkyblock.VSkyblock;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.UUID;

public class PhantomSpawn implements Listener {

    private VSkyblock plugin = VSkyblock.getInstance();

    @EventHandler
    public void onBedPlace(BlockPlaceEvent blockPlaceEvent) {
        Player player = blockPlaceEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        if (MaterialTags.BEDS.isTagged(blockPlaceEvent.getBlockPlaced())) {
            if (player.getWorld().getName().equals(IslandCacheHandler.playerislands.get(uuid))) {
                if (!plugin.getServer().getWorld(IslandCacheHandler.playerislands.get(uuid)).getGameRuleValue(GameRule.DO_INSOMNIA)) {
                    plugin.getServer().getWorld(IslandCacheHandler.playerislands.get(uuid)).setGameRule(GameRule.DO_INSOMNIA, true);
                }
            }
        }
    }
}
