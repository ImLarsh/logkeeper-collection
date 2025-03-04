
package com.collectionlog.listeners;

import com.collectionlog.CollectionLog;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class CollectionListener implements Listener {

    private final CollectionLog plugin;
    
    public CollectionListener(final CollectionLog plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handle item pickup events to track collection
     */
    @EventHandler
    public void onItemPickup(final EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        final ItemStack itemStack = event.getItem().getItemStack();
        this.plugin.getCollectionManager().addToCollection(player, itemStack);
    }
    
    /**
     * Handle item crafting events to track collection
     */
    @EventHandler
    public void onItemCraft(final CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        final ItemStack result = event.getCurrentItem();
        if (result == null || result.getType().isAir()) {
            return;
        }
        
        this.plugin.getCollectionManager().addToCollection(player, result);
    }
    
    /**
     * Handle fishing events to track collection
     */
    @EventHandler
    public void onPlayerFish(final PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH || !(event.getCaught() instanceof org.bukkit.entity.Item)) {
            return;
        }
        
        final org.bukkit.entity.Item caughtItem = (org.bukkit.entity.Item) event.getCaught();
        final ItemStack itemStack = caughtItem.getItemStack();
        
        this.plugin.getCollectionManager().addToCollection(event.getPlayer(), itemStack);
    }
    
    /**
     * Handle inventory click events in the collection log GUI
     */
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        final String title = event.getView().getTitle();
        
        // Check if the inventory is our GUI
        if (title.contains("Collection")) {
            event.setCancelled(true);
            
            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                this.plugin.getGuiManager().handleClick(player, event.getClickedInventory(), event.getSlot());
            }
        }
    }
    
    /**
     * Handle inventory close events for the collection log GUI
     */
    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        // Nothing special to do here yet, but could be used to clean up GUI state
    }
}
