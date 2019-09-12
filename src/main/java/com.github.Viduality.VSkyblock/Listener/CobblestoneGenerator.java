package com.github.Viduality.VSkyblock.Listener;

import com.github.Viduality.VSkyblock.VSkyblock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;


public class CobblestoneGenerator implements Listener {

    private VSkyblock plugin = VSkyblock.getInstance();

    public static HashMap<String, Integer> islandGenLevel = new HashMap<>(); //Islandname and generatorlevel
    public static HashMap<String, Double> generatorValues = new HashMap<>(); //"option" and value
    public static HashMap<String, Integer> islandlevels = new HashMap<>(); //Islandname and islandlevel

    @EventHandler
    public void cobblestoneGeneratorBlocks(BlockFormEvent blockFormEvent) {
        Block block = blockFormEvent.getNewState().getBlock();
        Material newmaterial = blockFormEvent.getNewState().getType();
        if (newmaterial.equals(Material.COBBLESTONE)) {
            if (islandGenLevel.containsKey(block.getLocation().getWorld().getName())) {
                int level = islandGenLevel.get(block.getLocation().getWorld().getName());
                switch (level) {
                    case 1:
                        blockFormEvent.getNewState().setType(rollCoalLevel());
                        break;
                    case 2:
                        blockFormEvent.getNewState().setType(rollIronLevel());
                        break;
                    case 3:
                        blockFormEvent.getNewState().setType(rollRedstoneLevel());
                        break;
                    case 4:
                        blockFormEvent.getNewState().setType(rollLapisLevel());
                        break;
                    case 5:
                        blockFormEvent.getNewState().setType(rollGoldLevel());
                        break;
                    case 6:
                        blockFormEvent.getNewState().setType(rollEmeraldLevel());
                        break;
                    case 7:
                        blockFormEvent.getNewState().setType(rollDiamondLevel());
                        break;
                }
            }
        }
    }

    @EventHandler
    public void cobblestoneGeneratorDrops(BlockBreakEvent blockBreakEvent) {
        if (blockBreakEvent.getBlock().getType().equals(Material.COBBLESTONE)) {
            if (plugin.getConfig().getString("CobblestoneGenerator.Cobblestone.MultipleDrops") != null) {
                if (plugin.getConfig().getString("CobblestoneGenerator.Cobblestone.MultipleDrops").equalsIgnoreCase("true")) {
                    if (blockBreakEvent.getBlock().getRelative(BlockFace.NORTH).getType().equals(Material.LAVA)
                            || blockBreakEvent.getBlock().getRelative(BlockFace.EAST).getType().equals(Material.LAVA)
                            || blockBreakEvent.getBlock().getRelative(BlockFace.SOUTH).getType().equals(Material.LAVA)
                            || blockBreakEvent.getBlock().getRelative(BlockFace.WEST).getType().equals(Material.LAVA)) {
                        if (blockBreakEvent.getBlock().getRelative(BlockFace.NORTH).getType().equals(Material.WATER)
                                || blockBreakEvent.getBlock().getRelative(BlockFace.EAST).getType().equals(Material.WATER)
                                || blockBreakEvent.getBlock().getRelative(BlockFace.SOUTH).getType().equals(Material.WATER)
                                || blockBreakEvent.getBlock().getRelative(BlockFace.WEST).getType().equals(Material.WATER)) {
                            if (islandlevels.containsKey(blockBreakEvent.getBlock().getLocation().getWorld().getName())) {
                                int level = islandlevels.get(blockBreakEvent.getBlock().getLocation().getWorld().getName());
                                if (level >= generatorValues.get("CobblestoneLevelIntervall")) {
                                    int additionalDropsAmount = rollCobbleAmount(blockBreakEvent.getBlock().getLocation().getWorld().getName());
                                    if (additionalDropsAmount != 0) {
                                        blockBreakEvent.getBlock().getLocation().getWorld().dropItemNaturally(blockBreakEvent.getBlock().getLocation(), new ItemStack(Material.COBBLESTONE, additionalDropsAmount));
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Material rollCoalLevel() {
        double random = Math.random();
        double chance = generatorValues.get("CoalChance") / 100;
        if (chance >= random) {
            return Material.COAL_ORE;
        } else {
            return Material.COBBLESTONE;
        }
    }

    private Material rollIronLevel() {
        double random = Math.random();
        double chanceIron = generatorValues.get("IronChance") / 100;
        double chanceCoal = (generatorValues.get("CoalChance") / 100) + chanceIron;
        if (chanceIron >= random) {
            return Material.IRON_ORE;
        } else if (chanceCoal >= random){
            return Material.COAL_ORE;
        } else {
            return Material.COBBLESTONE;
        }
    }

    private Material rollRedstoneLevel() {
        double random = Math.random();
        double chanceRedstone = generatorValues.get("RedstoneChance") / 100;
        double chanceIron = (generatorValues.get("IronChance") / 100) + chanceRedstone;
        double chanceCoal = (generatorValues.get("CoalChance") / 100) + chanceIron;
        if (chanceRedstone >= random) {
            return Material.REDSTONE_ORE;
        } else if (chanceIron >= random) {
            return Material.IRON_ORE;
        } else if (chanceCoal >= random){
            return Material.COAL_ORE;
        } else {
            return Material.COBBLESTONE;
        }
    }

    private Material rollLapisLevel() {
        double random = Math.random();
        double chanceLapis = generatorValues.get("LapisChance") / 100;
        double chanceRedstone = (generatorValues.get("RedstoneChance") / 100) + chanceLapis;
        double chanceIron = (generatorValues.get("IronChance") / 100) + chanceRedstone;
        double chanceCoal = (generatorValues.get("CoalChance") / 100) + chanceIron;
        if (chanceLapis >= random) {
            return Material.LAPIS_ORE;
        } else if (chanceRedstone >= random) {
            return Material.REDSTONE_ORE;
        } else if (chanceIron >= random) {
            return Material.IRON_ORE;
        } else if (chanceCoal >= random){
            return Material.COAL_ORE;
        } else {
            return Material.COBBLESTONE;
        }
    }

    private Material rollGoldLevel() {
        double random = Math.random();
        double chanceGold = generatorValues.get("GoldChance") / 100;
        double chanceLapis = (generatorValues.get("LapisChance") / 100) + chanceGold;
        double chanceRedstone = (generatorValues.get("RedstoneChance") / 100) + chanceLapis;
        double chanceIron = (generatorValues.get("IronChance") / 100) + chanceRedstone;
        double chanceCoal = (generatorValues.get("CoalChance") / 100) + chanceIron;
        if (chanceGold >= random) {
            return Material.GOLD_ORE;
        } else if (chanceLapis >= random) {
            return Material.LAPIS_ORE;
        } else if (chanceRedstone >= random) {
            return Material.REDSTONE_ORE;
        } else if (chanceIron >= random){
            return Material.IRON_ORE;
        } else if (chanceCoal >= random){
            return Material.COAL_ORE;
        } else {
            return Material.COBBLESTONE;
        }
    }

    private Material rollEmeraldLevel() {
        double random = Math.random();
        double chanceEmerald = generatorValues.get("EmeraldChance") / 100;
        double chanceGold = (generatorValues.get("GoldChance") / 100) + chanceEmerald;
        double chanceLapis = (generatorValues.get("LapisChance") / 100) + chanceGold;
        double chanceRedstone = (generatorValues.get("RedstoneChance") / 100) + chanceLapis;
        double chanceIron = (generatorValues.get("IronChance") / 100) + chanceRedstone;
        double chanceCoal = (generatorValues.get("CoalChance") / 100) + chanceIron;
        if (chanceEmerald >= random) {
            return Material.EMERALD_ORE;
        } else if (chanceGold >= random) {
            return Material.GOLD_ORE;
        } else if (chanceLapis >= random) {
            return Material.LAPIS_ORE;
        } else if (chanceRedstone >= random) {
            return Material.REDSTONE_ORE;
        } else if (chanceIron >= random){
            return Material.IRON_ORE;
        } else if (chanceCoal >= random){
            return Material.COAL_ORE;
        } else {
            return Material.COBBLESTONE;
        }
    }

    private Material rollDiamondLevel() {
        double random = Math.random();
        double chanceDiamond = generatorValues.get("DiamondChance") / 100;
        double chanceEmerald = (generatorValues.get("EmeraldChance") /100) + chanceDiamond;
        double chanceGold = (generatorValues.get("GoldChance") / 100) + chanceEmerald;
        double chanceLapis = (generatorValues.get("LapisChance") / 100) + chanceGold;
        double chanceRedstone = (generatorValues.get("RedstoneChance") / 100) + chanceLapis;
        double chanceIron = (generatorValues.get("IronChance") / 100) + chanceRedstone;
        double chanceCoal = (generatorValues.get("CoalChance") / 100) + chanceIron;
        if (chanceDiamond >= random) {
            return Material.DIAMOND_ORE;
        } else if (chanceEmerald >= random) {
            return Material.EMERALD_ORE;
        } else if (chanceGold >= random) {
            return Material.GOLD_ORE;
        } else if (chanceLapis >= random) {
            return Material.LAPIS_ORE;
        } else if (chanceRedstone >= random) {
            return Material.REDSTONE_ORE;
        } else if (chanceIron >= random){
            return Material.IRON_ORE;
        } else if (chanceCoal >= random){
            return Material.COAL_ORE;
        } else {
            return Material.COBBLESTONE;
        }
    }

    private int rollCobbleAmount(String island) {
        double chance = generatorValues.get("CobblestoneChance") / 100;
        int chances = (int) (islandlevels.get(island) / generatorValues.get("CobblestoneLevelIntervall"));
        int drops = 0;
        for (int i = 0; i < chances; i++) {
            double random = Math.random();
            if (chance >= random) {
                drops = drops +1;
            }
        }
        return drops;
    }
}