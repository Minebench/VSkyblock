package com.github.Viduality.VSkyblock.Commands;

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
import com.github.Viduality.VSkyblock.Utilitys.PlayerInfo;
import com.github.Viduality.VSkyblock.Utilitys.IslandOptionsCache;
import com.github.Viduality.VSkyblock.VSkyblock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/*
 * Shows the island options menu.
 */
public class IslandOptions extends PlayerSubCommand {

    public IslandOptions(VSkyblock plugin) {
        super(plugin, "options", "option", "settings", "setting");
    }

    @Override
    public void execute(CommandSender sender, PlayerInfo playerInfo, String[] args) {
        if (playerInfo.getIslandId() != 0) {
            plugin.getDb().getReader().getIslandOptions(playerInfo.getIslandId(), isoptionsCache ->
                    createOptionsInventory(isoptionsCache, playerInfo.getPlayer(), playerInfo.isIslandOwner()));
        } else {
            ConfigShorts.messagefromString("NoIsland", playerInfo.getPlayer());
        }
    }

    /**
     * Creates the options inventory and opens it for the player.
     * @param cache
     * @param player
     * @param islandOwner
     */
    private void createOptionsInventory(IslandOptionsCache cache, Player player, boolean islandOwner) {
        String difficulty = cache.getDifficulty();
        boolean visit = cache.getVisit();
        boolean needRequest = cache.getNeedRequest();
        Inventory isOptions = Bukkit.createInventory(null, 18, getInvName());

        //VISIT

        Material visitBlock;
        if (visit) {
            if (needRequest) {
                visitBlock = getMaterial(ConfigShorts.getOptionsConfig().getString("Visit.NeedsRequestItem"));
            } else {
                visitBlock = getMaterial(ConfigShorts.getOptionsConfig().getString("Visit.AllowedItem"));
            }
        } else {
            visitBlock = getMaterial(ConfigShorts.getOptionsConfig().getString("Visit.NotAllowedItem"));
        }
        ItemStack visitSlot = new ItemStack(visitBlock, 1);
        ItemMeta visitSlotItemMeta = visitSlot.getItemMeta();
        if (visitSlotItemMeta != null) {
            if (visit) {
                if (needRequest) {
                    visitSlotItemMeta.setDisplayName(getDisplayNameVisit("NeedsRequest"));
                } else {
                    visitSlotItemMeta.setDisplayName(getDisplayNameVisit("Allowed"));
                }
            } else {
                visitSlotItemMeta.setDisplayName(getDisplayNameVisit("NotAllowed"));
            }
            visitSlotItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            visitSlotItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            visitSlotItemMeta.addItemFlags(ItemFlag.values());
        }

        visitSlot.setItemMeta(visitSlotItemMeta);
        isOptions.setItem(2, visitSlot);

        //DIFFICULTY

        Material difficultyBlock;
        switch (difficulty) {
            case "EASY":
                difficultyBlock = getMaterial(ConfigShorts.getOptionsConfig().getString("Difficulty.EasyItem"));
                break;
            case "HARD":
                difficultyBlock = getMaterial(ConfigShorts.getOptionsConfig().getString("Difficulty.HardItem"));
                break;
            default:
                difficultyBlock = getMaterial(ConfigShorts.getOptionsConfig().getString("Difficulty.NormalItem"));
                break;
        }
        ItemStack difficultySlot = new ItemStack(difficultyBlock, 1);
        ItemMeta difficultySlotItemMeta = difficultySlot.getItemMeta();
        if (difficultySlotItemMeta != null) {
            switch (difficulty) {
                case "EASY":
                    difficultySlotItemMeta.setDisplayName(getDisplayNameDifficulty("Easy"));
                    break;
                case "HARD":
                    difficultySlotItemMeta.setDisplayName(getDisplayNameDifficulty("Hard"));
                    break;
                default:
                    difficultySlotItemMeta.setDisplayName(getDisplayNameDifficulty("Normal"));
                    break;
            }
            difficultySlotItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            difficultySlotItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            difficultySlotItemMeta.addItemFlags(ItemFlag.values());
        }
        difficultySlot.setItemMeta(difficultySlotItemMeta);
        isOptions.setItem(6, difficultySlot);

        //COBBLESTONEGENERATOR

        ItemStack generatorSlot = new ItemStack(Material.COBBLESTONE, 1);
        ItemMeta generatorSlotItemMeta = difficultySlot.getItemMeta();
        if (generatorSlotItemMeta != null) {

            generatorSlotItemMeta.setDisplayName(getDisplayNameGenerator());

            generatorSlotItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            generatorSlotItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            generatorSlotItemMeta.addItemFlags(ItemFlag.values());
        }
        generatorSlot.setItemMeta(generatorSlotItemMeta);
        isOptions.setItem(4, generatorSlot);

        //CONFIRM BUTTON

        if (islandOwner) {
            Material acceptBlock = Material.LIME_WOOL;
            ItemStack acceptBlockSlot = new ItemStack(acceptBlock, 1);
            acceptBlockSlot.editMeta((acceptBlockSlotItemMeta) -> {
                acceptBlockSlotItemMeta.setDisplayName(getAccept());
                acceptBlockSlotItemMeta.setEnchantmentGlintOverride(true);
                acceptBlockSlotItemMeta.addItemFlags(ItemFlag.values());
            });
            isOptions.setItem(17, acceptBlockSlot);
        }
        player.openInventory(isOptions);
    }

    /**
     * Checks given material and returns it if existing.
     *
     * @param material
     * @return Material
     */
    private Material getMaterial(String material) {
        if (Material.matchMaterial(material) != null) {
            return Material.getMaterial(material);
        } else {
            return Material.BARRIER;
        }
    }

    /**
     * Returns the options inventorys name.
     *
     * @return String
     */
    private String getInvName() {
        if (ConfigShorts.getOptionsConfig().getString("InventoryName") != null) {
            return ConfigShorts.getOptionsConfig().getString("InventoryName");
        } else {
            return "Island options";
        }
    }

    /**
     * Checks given string and returns it if difficulty enum exists.
     *
     * @param difficulty
     * @return String
     */
    private String getDisplayNameDifficulty(String difficulty) {
        String displayname = ConfigShorts.getOptionsConfig().getString("Difficulty." + difficulty);
        if (displayname != null) {
            return displayname;
        } else {
            return difficulty;
        }
    }

    /**
     * Checks given string and returns it if not null.
     *
     * @param allowed
     * @return String
     */
    private String getDisplayNameVisit(String allowed) {
        String displayname = ConfigShorts.getOptionsConfig().getString("Visit." + allowed);
        if (displayname != null) {
            return displayname;
        } else {
            return "Visitors allowed";
        }
    }

    /**
     * Checks given string and returns it if not null.
     *
     * @return String
     */
    private String getDisplayNameGenerator() {
        String displayname = ConfigShorts.getOptionsConfig().getString("CobblestoneGenerator.DisplayName");
        if (displayname != null) {
            return displayname;
        } else {
            return "Cobblestone-Generator";
        }
    }

    /**
     * Returns the accept string for the island options.
     *
     * @return String
     */
    private String getAccept() {
        String accept = ConfigShorts.getOptionsConfig().getString("Accept");
        if (accept != null) {
            return accept;
        } else {
            return "Accept";
        }
    }
}
