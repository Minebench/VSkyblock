package com.github.Viduality.VSkyblock.Listener;
/*
 * VSkyblock
 * Copyright (c) 2024 Max Lee aka Phoenix616 (max@themoep.de)
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

import com.github.Viduality.VSkyblock.Commands.IslandLevel;
import com.github.Viduality.VSkyblock.Utilitys.ConfigShorts;
import com.github.Viduality.VSkyblock.Utilitys.IslandCacheHandler;
import org.bukkit.Material;
import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Random;

public class EndermanHandler implements Listener {

    private static final Random RANDOM = new Random();

    @EventHandler(ignoreCancelled = true)
    public void onEndermanSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Enderman enderman && enderman.getCarriedBlock() == null) {
            String worldName = event.getLocation().getWorld().getName();
            if (IslandCacheHandler.playerislands.containsValue(worldName)) {
                IslandLevel.IslandCounter count = IslandCacheHandler.islandCounts.get(worldName);
                if (count != null) {
                    if (count.blockCounts.getOrDefault(Material.GRASS_BLOCK, 0) == 0
                            && RANDOM.nextDouble() < ConfigShorts.getDefConfig().getDouble("EndermanGrassBlockChance")) {
                        enderman.setCarriedBlock(Material.GRASS_BLOCK.createBlockData());
                    }
                } else {
                    IslandCacheHandler.islandCounts.put(worldName, IslandLevel.calculate(event.getLocation().getWorld(), 0, c -> {}));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEndermanDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Enderman enderman && enderman.getCarriedBlock() != null
                && enderman.getCarriedBlock().getMaterial() == Material.GRASS_BLOCK) {
            String worldName = event.getEntity().getWorld().getName();
            IslandLevel.IslandCounter count = IslandCacheHandler.islandCounts.get(worldName);
            if (count != null) {
                Integer previous = count.blockCounts.putIfAbsent(Material.GRASS_BLOCK, 1);
                if (previous != null && previous == 0) {
                    count.blockCounts.put(Material.GRASS_BLOCK, 1);
                }
            }
        }
    }
}
