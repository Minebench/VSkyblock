package com.github.Viduality.VSkyblock.Listener;

import com.github.Viduality.VSkyblock.Utilitys.IslandCacheHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TradingHandler implements Listener {

    private static final Map<Material, Function<Location, Boolean>> TRADE_LIMITER_MAP = new HashMap<>();

    static {
        TRADE_LIMITER_MAP.put(Material.POINTED_DRIPSTONE,
                l -> IslandCacheHandler.islandlevels.containsKey(l.getWorld().getName())
                        && IslandCacheHandler.islandlevels.get(l.getWorld().getName()) < 100);
    }

    @EventHandler
    public void onTraderSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Merchant) {
            List<MerchantRecipe> newTrades = new ArrayList<>();
            for (MerchantRecipe trade : ((Merchant) event.getEntity()).getRecipes()) {
                if (!isTradeLimited(event.getLocation(), trade)) {
                    newTrades.add(trade);
                }
            }
            // if it changed then set the new trades
            if (newTrades.size() != ((Merchant) event.getEntity()).getRecipeCount()) {
                ((Merchant) event.getEntity()).setRecipes(newTrades);
            }
        }
    }

    @EventHandler
    public void onTrade(TradeSelectEvent event) {
        if (isTradeLimited(event.getMerchant() instanceof Entity
                ? ((Entity) event.getMerchant()).getLocation() : event.getWhoClicked().getLocation(),
                event.getMerchant().getRecipe(event.getIndex()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTradeAcquire(VillagerAcquireTradeEvent event) {
        if (isTradeLimited(event.getEntity().getLocation(), event.getRecipe())) {
            event.setCancelled(true);
        }
    }

    private boolean isTradeLimited(Location location, MerchantRecipe recipe) {
        if (recipe != null) {
            Function<Location, Boolean> limiter = TRADE_LIMITER_MAP.get(recipe.getResult().getType());
            if (limiter != null) {
                return limiter.apply(location);
            }
        }
        return false;
    }

}
