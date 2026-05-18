package com.github.Viduality.VSkyblock;


/*
 * VSkyblock
 * Copyright (c) 2026 Max Lee aka Phoenix616 (max@themoep.de)
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

import com.github.Viduality.VSkyblock.Challenges.Challenge;
import com.github.Viduality.VSkyblock.Utilitys.IslandCacheHandler;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Locale;

public class LuckPermsContextProvider {
    private final VSkyblock plugin;

    public LuckPermsContextProvider(VSkyblock plugin) {
        this.plugin = plugin;
        RegisteredServiceProvider<LuckPerms> provider = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            provider.getProvider().getContextManager().registerCalculator(new SkyblockIslandChallengeDifficultyContextCalculator());
        } else {
            plugin.getLogger().severe("LuckPerms is installed but we couldn't get the API via the ServiceManager? Contexts will not work!");
        }
    }

    private class SkyblockIslandChallengeDifficultyContextCalculator implements ContextCalculator<Player> {
        @Override
        public void calculate(Player target, ContextConsumer consumer) {
            Challenge.Difficulty difficulty = IslandCacheHandler.islandChallengeDifficulty.get(target.getWorld().getName());
            consumer.accept("island-challenge-difficulty", difficulty != null ? difficulty.name().toLowerCase(Locale.ROOT) : "none");
        }

        @Override
        public ContextSet estimatePotentialContexts() {
            ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
            builder.add("island-challenge-difficulty", "none");
            for (Challenge.Difficulty difficulty : Challenge.Difficulty.values()) {
                builder.add("island-challenge-difficulty", difficulty.name().toLowerCase(Locale.ROOT));
            }
            return builder.build();
        }
    }
}
