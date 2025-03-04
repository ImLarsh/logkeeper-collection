
package com.collectionlog.listeners;

import com.collectionlog.CollectionLog;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final CollectionLog plugin;
    
    public PlayerListener(final CollectionLog plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Load a player's collection data when they join
     */
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.plugin.getCollectionManager().loadPlayerData(event.getPlayer().getUniqueId());
    }
    
    /**
     * Save a player's collection data when they leave
     */
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.plugin.getCollectionManager().savePlayerData(event.getPlayer().getUniqueId());
    }
}
