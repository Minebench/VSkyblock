package com.github.Viduality.VSkyblock.Challenges;

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

import com.github.Viduality.VSkyblock.Utilitys.ConfigShorts;
import com.github.Viduality.VSkyblock.VSkyblock;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChallengesManager {

    public static final Map<String, Challenge> challenges = new HashMap<>();
    static final Map<String, Challenge> challengesEasy = new HashMap<>(); //DisplayName and challenge
    static final Map<String, Challenge> challengesMedium = new HashMap<>(); //DisplayName and challenge
    static final Map<String, Challenge> challengesHard = new HashMap<>(); //DisplayName and challenge
    public static List<Challenge> sortedChallengesEasy = new ArrayList<>();
    public static List<Challenge> sortedChallengesMedium = new ArrayList<>();
    public static List<Challenge> sortedChallengesHard = new ArrayList<>();

    public static Cache<UUID, Integer> onIslandDelay = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    public static Cache<UUID, Integer> islandLevelDelay = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();
    
    private final VSkyblock plugin;

    private final ChallengesInventoryCreator inventoryCreator;

    public ChallengesManager(VSkyblock plugin) {
        this.plugin = plugin;
        inventoryCreator = new ChallengesInventoryCreator(plugin);
    }

    public ChallengesInventoryCreator getInventoryCreator() {
        return inventoryCreator;
    }

    /**
     * Checks if a challenge can be completed.
     * Checks if a player or island does match the requirements for the challenge and executes all needed updates.
     * @param challenge    The Challenge.
     * @param player       The player who wants to complete the challenge.
     */
    public void checkChallenge(Challenge challenge, Player player, Inventory inv, int challengeSlot) {
        plugin.getDb().getReader().getIslandIdFromPlayer(player.getUniqueId(), (islandid) -> plugin.getDb().getReader().getIslandChallenges(islandid, (islandChallenges) -> {
            boolean repeat = islandChallenges.getChallengeCount(challenge.getMySQLKey()) != 0;

            if (challenge.getChallengeType().equals(Challenge.ChallengeType.ON_PLAYER)) {
                boolean enoughItems = true;
                for (ItemStack i : challenge.getNeededItems()) {
                    int neededamount = i.getAmount();
                    for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
                        ItemStack item = player.getInventory().getItem(slot);
                        if (item != null) {
                            if (item.getType().equals(i.getType())) {
                                neededamount = neededamount - item.getAmount();
                            }
                        }
                    }
                    if (neededamount > 0) {
                        enoughItems = false;
                    }
                }
                if (player.getGameMode().equals(GameMode.CREATIVE)) {
                    enoughItems = true;
                }
                if (enoughItems) {
                    List<ItemStack> rewards;
                    if (repeat) {
                        rewards = challenge.getRepeatRewards();
                    } else {
                        rewards = challenge.getRewards();
                    }
                    if (getEmptySlotCount(player.getInventory(), challenge.getNeededItems()) >= rewards.size()) {
                        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                            clearItems(player.getInventory(), challenge.getNeededItems());
                        }
                        giveRewards(player.getInventory(), rewards);
                        plugin.getDb().getWriter().updateChallengeCount(islandid, challenge.getMySQLKey(), islandChallenges.getChallengeCount(challenge.getMySQLKey()) + 1);
                        inv.setItem(challengeSlot, inventoryCreator.createChallengeItem(challenge, islandChallenges.getChallengeCount(challenge.getMySQLKey()) + 1, islandChallenges.getTrackedChallenges().contains(challenge.getMySQLKey())));
                        unTrack(challenge, player);
                        if (!repeat) {
                            notifyChallengeCompleteFirst(challenge, player);
                        } else if (ConfigShorts.getDefConfig().isConfigurationSection("ChallengeComplete")) {
                            try {
                                Sound sound = Sound.valueOf(ConfigShorts.getDefConfig().getString("ChallengeComplete.Sound", "SoundNotFound").toUpperCase());
                                float pitch = (float) ConfigShorts.getDefConfig().getDouble("ChallengeComplete.Pitch");
                                player.playSound(player.getLocation(), sound, 1, pitch);
                            } catch (IllegalArgumentException e) {
                                VSkyblock.getInstance().getLogger().warning("ChallengeComplete sound is invalid! " + e.getMessage());
                            }
                        }
                    } else {
                        ConfigShorts.messagefromString("NotEnoughInventorySpace", player);
                        player.closeInventory();
                    }
                } else {
                    ConfigShorts.messagefromString("NotEnoughItems", player);
                    player.closeInventory();
                }
            } else if (challenge.getChallengeType().equals(Challenge.ChallengeType.ISLAND_LEVEL)) {
                if (!repeat) {
                    if (!islandLevelDelay.asMap().containsKey(player.getUniqueId())) {
                        islandLevelDelay.put(player.getUniqueId(), 1);
                        plugin.getDb().getReader().getIslandLevelFromUuid(player.getUniqueId(), (islandLevel) -> {
                            if (islandLevel >= challenge.getNeededLevel()) {
                                if (getEmptySlotCount(player.getInventory(), Collections.emptyList()) >= challenge.getRewards().size()) {
                                    giveRewards(player.getInventory(), challenge.getRewards());
                                    notifyChallengeCompleteFirst(challenge, player);
                                    plugin.getDb().getWriter().updateChallengeCount(islandid, challenge.getMySQLKey(), islandChallenges.getChallengeCount(challenge.getMySQLKey()) + 1);
                                    inv.setItem(challengeSlot, inventoryCreator.createChallengeItem(challenge, islandChallenges.getChallengeCount(challenge.getMySQLKey()) + 1, islandChallenges.getTrackedChallenges().contains(challenge.getMySQLKey())));
                                } else {
                                    ConfigShorts.messagefromString("NotEnoughInventorySpace", player);
                                }
                            } else {
                                ConfigShorts.messagefromString("IslandLevelNotHighEnough", player);
                            }
                        });
                    } else {
                        ConfigShorts.messagefromString("AlreadyCheckedIsland", player);
                    }
                } else {
                    ConfigShorts.messagefromString("ChallengeNotRepeatable", player);
                    inv.setItem(challengeSlot, inventoryCreator.createChallengeItem(challenge, islandChallenges.getChallengeCount(challenge.getMySQLKey()) + 1, islandChallenges.getTrackedChallenges().contains(challenge.getMySQLKey())));
                }
            } else if (challenge.getChallengeType().equals(Challenge.ChallengeType.ON_ISLAND)) {
                if (!repeat) {
                    if (!onIslandDelay.asMap().containsKey(player.getUniqueId())) {
                        onIslandDelay.put(player.getUniqueId(), 1);

                        ConfigShorts.messagefromString("CheckingIslandForChallenge", player);
                        HashMap<Material, Integer> result = getBlocks(player, challenge.getRadius());

                        boolean enoughItems = true;
                        for (ItemStack stack : challenge.getNeededItems()) {
                            if (result.containsKey(stack.getType())) {
                                if (result.get(stack.getType()) < stack.getAmount()) {
                                    enoughItems = false;
                                }
                            } else {
                                enoughItems = false;
                            }
                        }
                        if (enoughItems) {
                            if (getEmptySlotCount(player.getInventory(), challenge.getNeededItems()) >= challenge.getRewards().size()) {
                                giveRewards(player.getInventory(), challenge.getRewards());
                                notifyChallengeCompleteFirst(challenge, player);
                                plugin.getDb().getWriter().updateChallengeCount(islandid, challenge.getMySQLKey(), islandChallenges.getChallengeCount(challenge.getMySQLKey()) + 1);
                                inv.setItem(challengeSlot, inventoryCreator.createChallengeItem(challenge, islandChallenges.getChallengeCount(challenge.getMySQLKey()) + 1, islandChallenges.getTrackedChallenges().contains(challenge.getMySQLKey())));
                            } else {
                                ConfigShorts.messagefromString("NotEnoughInventorySpace", player);
                            }
                        } else {
                            ConfigShorts.messagefromString("IslandDoesNotMatchRequirements", player);
                        }
                    } else {
                        ConfigShorts.messagefromString("AlreadyCheckedIsland", player);
                    }
                } else {
                    ConfigShorts.messagefromString("ChallengeNotRepeatable", player);
                    inv.setItem(challengeSlot, inventoryCreator.createChallengeItem(challenge, islandChallenges.getChallengeCount(challenge.getMySQLKey()) + 1, islandChallenges.getTrackedChallenges().contains(challenge.getMySQLKey())));
                }
            }
        }));
    }

    public void notifyChallengeCompleteFirst(Challenge challenge, Player player) {
        ConfigShorts.broadcastChallengeCompleted("ChallengeComplete", player.getName(), challenge);
        if (ConfigShorts.getDefConfig().isConfigurationSection("ChallengeCompleteFirst")) {
            try {
                Sound sound = Sound.valueOf(ConfigShorts.getDefConfig().getString("ChallengeCompleteFirst.Sound").toUpperCase());
                float pitch = (float) ConfigShorts.getDefConfig().getDouble("ChallengeCompleteFirst.Pitch");
                for (Player p : player.getWorld().getPlayers()) {
                    p.playSound(p.getLocation(), sound, 1, pitch);
                }
            } catch (IllegalArgumentException e) {
                VSkyblock.getInstance().getLogger().warning("ChallengeCompleteFirst sound is invalid! " + e.getMessage());
            }
        }
    }

    /**
     * Removes a given list of item stacks from the given inventory.
     * @param inv    The inventory.
     * @param items  The list of items to be removed
     */
    private void clearItems(Inventory inv, List<ItemStack> items) {
        for (ItemStack itemToRemove : items) {
            int amount = itemToRemove.getAmount();
            for (int slot = 0; slot < inv.getSize(); slot++) {
                ItemStack stackInInv = inv.getItem(slot);
                if (stackInInv != null) {
                    if (stackInInv.getType().equals(itemToRemove.getType())) {
                        int newAmount = stackInInv.getAmount() - amount;
                        if (newAmount > 0) {
                            stackInInv.setAmount(newAmount);
                            break;
                        } else {
                            inv.clear(slot);
                            amount = amount - stackInInv.getAmount();
                            if (amount == 0) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Counts how many slots would be freed up when the required items are removed
     * @param inv    The inventory
     * @param items  The list of items that would be removed
     */
    private int countExtraFreeSlots(Inventory inv, List<ItemStack> items) {
        int freeSlots = 0;
        for (ItemStack itemToRemove : items) {
            int amount = itemToRemove.getAmount();
            for (int slot = 0; slot < inv.getSize(); slot++) {
                ItemStack stackInInv = inv.getItem(slot);
                if (stackInInv != null) {
                    if (stackInInv.getType().equals(itemToRemove.getType())) {
                        int newAmount = stackInInv.getAmount() - amount;
                        if (newAmount <= 0) {
                            freeSlots++;
                            amount = amount - stackInInv.getAmount();
                            if (amount == 0) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return freeSlots;
    }

    /**
     * Gives the rewards for the challenge.
     *
     * @param inv
     * @param items
     */
    private void giveRewards(Inventory inv, List<ItemStack> items) {
        for (ItemStack current : items) {
            inv.addItem(current.clone());
        }
    }

    /**
     * Returns the amount empty slots in an inventory.
     *
     * @param inv The inventory to search in
     */
    private int getEmptySlotCount(Inventory inv, List<ItemStack> items) {
        int counter = 0;
        for (int currentSlot = 0; currentSlot < 36; currentSlot++) {
            if (inv.getItem(currentSlot) == null) {
                counter++;
            }
        }
        counter += countExtraFreeSlots(inv, items);
        return counter;
    }

    /**
     * Get all blocks in a specific radius around the player.
     *
     * @param player
     * @param radius
     * @return The block type counts
     */
    private HashMap<Material, Integer> getBlocks(Player player, Integer radius) {
        HashMap<Material, Integer> blocks = new HashMap<>();
        Location loc = player.getLocation();
        int blockx = (int) loc.getX();
        int blocky = (int) loc.getY();
        int blockz = (int) loc.getZ();

        for (int x = blockx - radius; x < blockx + radius; x++ ) {
            for (int z = blockz - radius; z < blockz + radius; z++) {
                if (player.getWorld().isChunkLoaded(x >> 4, z >> 4)) {
                    for (int y = blocky - radius; y < blocky + radius; y++) {
                        Material blockType = player.getWorld().getBlockAt(x, y, z).getType();
                        if (!blockType.isAir()) {
                            blocks.put(blockType, blocks.getOrDefault(blockType, 0) + 1);
                        }
                    }
                }
            }
        }
        return blocks;
    }

    public void toggleTracked(Challenge challenge, Player player) {
        plugin.getDb().getReader().getIslandIdFromPlayer(player.getUniqueId(), (islandId) -> plugin.getDb().getReader().getIslandChallenges(islandId, (challenges) -> {
            if (challenges.getTrackedChallenges().size() < 10
                    && challenges.getChallengeCount(challenge.getChallengeName()) == 0 || challenge.getRepeatRewards() != null) {
                if (challenges.getTrackedChallenges().contains(challenge.getMySQLKey())) {
                    challenges.removeTrackedChallenge(challenge.getMySQLKey());
                    plugin.getDb().getWriter().updateChallengeTracked(islandId, challenge.getMySQLKey(), false);
                } else {
                    challenges.addTrackedChallenge(challenge.getMySQLKey());
                    plugin.getDb().getWriter().updateChallengeTracked(islandId, challenge.getMySQLKey(), true);
                }
                VSkyblock.getInstance().getScoreboardManager().updateTracked(islandId, challenges);
            }
        }));
    }

    public void unTrack(Challenge challenge, Player player) {
        plugin.getDb().getReader().getIslandIdFromPlayer(player.getUniqueId(), (islandId) -> plugin.getDb().getReader().getIslandChallenges(islandId, (challenges) -> {
            if (challenges.removeTrackedChallenge(challenge.getMySQLKey())) {
                plugin.getDb().getWriter().updateChallengeTracked(islandId, challenge.getMySQLKey(), false);
                VSkyblock.getInstance().getScoreboardManager().updateTracked(islandId, challenges);
            }
        }));
    }
}
