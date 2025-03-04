
package com.collectionlog.manager;

import com.collectionlog.CollectionLog;
import com.collectionlog.model.CollectionItem;
import com.collectionlog.model.ItemCategory;
import com.collectionlog.model.PlayerCollection;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class CollectionManager {

    private final CollectionLog plugin;
    
    @Getter
    private final Map<String, ItemCategory> categories;
    
    private final Map<UUID, PlayerCollection> playerCollections;
    private final File dataFolder;
    
    public CollectionManager(final CollectionLog plugin) {
        this.plugin = plugin;
        this.categories = new LinkedHashMap<>();
        this.playerCollections = new HashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        
        if (!this.dataFolder.exists() && !this.dataFolder.mkdirs()) {
            plugin.getLogger().severe("Failed to create data directory!");
        }
        
        this.loadCategories();
        
        // Load online players' data if plugin is enabled after players joined
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.loadPlayerData(player.getUniqueId());
        }
    }
    
    /**
     * Load all categories from the items.yml configuration
     */
    public void loadCategories() {
        this.categories.clear();
        
        final ConfigurationSection categoriesSection = this.plugin.getConfigManager()
                .getItemsConfig().getConfigurationSection("categories");
        
        if (categoriesSection == null) {
            this.plugin.getLogger().warning("No categories found in items.yml");
            return;
        }
        
        for (String categoryId : categoriesSection.getKeys(false)) {
            final ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryId);
            if (categorySection != null) {
                final ItemCategory category = ItemCategory.fromConfig(categoryId, categorySection);
                if (category != null) {
                    this.categories.put(categoryId, category);
                    this.plugin.debug("Loaded category: " + categoryId + " with " + 
                            category.getItems().size() + " items");
                }
            }
        }
        
        this.plugin.getLogger().info("Loaded " + this.categories.size() + " categories with " + 
                this.categories.values().stream().mapToInt(cat -> cat.getItems().size()).sum() + " items");
    }
    
    /**
     * Get a player's collection data
     * 
     * @param playerId The UUID of the player
     * @return The player's collection data
     */
    public PlayerCollection getPlayerCollection(final UUID playerId) {
        return this.playerCollections.computeIfAbsent(playerId, id -> {
            final File playerFile = this.getPlayerDataFile(id);
            return PlayerCollection.fromFile(id, playerFile);
        });
    }
    
    /**
     * Load a player's collection data
     * 
     * @param playerId The UUID of the player
     */
    public void loadPlayerData(final UUID playerId) {
        final File playerFile = this.getPlayerDataFile(playerId);
        final PlayerCollection collection = PlayerCollection.fromFile(playerId, playerFile);
        this.playerCollections.put(playerId, collection);
        this.plugin.debug("Loaded collection data for player: " + playerId);
    }
    
    /**
     * Save a player's collection data
     * 
     * @param playerId The UUID of the player
     */
    public void savePlayerData(final UUID playerId) {
        final PlayerCollection collection = this.playerCollections.get(playerId);
        if (collection == null) {
            return;
        }
        
        try {
            collection.saveToFile(this.getPlayerDataFile(playerId));
            this.plugin.debug("Saved collection data for player: " + playerId);
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save player data: " + playerId, e);
        }
    }
    
    /**
     * Save all player collection data
     */
    public void saveAllData() {
        for (UUID playerId : this.playerCollections.keySet()) {
            this.savePlayerData(playerId);
        }
        this.plugin.debug("Saved all player collection data");
    }
    
    /**
     * Get the file for a player's data
     * 
     * @param playerId The UUID of the player
     * @return The file for the player's data
     */
    private File getPlayerDataFile(final UUID playerId) {
        return new File(this.dataFolder, playerId.toString() + ".yml");
    }
    
    /**
     * Check if a player has an item in their collection
     * 
     * @param playerId The UUID of the player
     * @param categoryId The category ID
     * @param itemId The item ID
     * @return True if the player has the item
     */
    public boolean hasCollected(final UUID playerId, final String categoryId, final String itemId) {
        final PlayerCollection collection = this.getPlayerCollection(playerId);
        return collection.hasCollected(categoryId, itemId);
    }
    
    /**
     * Add an item to a player's collection
     * 
     * @param player The player
     * @param itemStack The item to add
     * @return True if the item was newly added to the collection
     */
    public boolean addToCollection(final Player player, final ItemStack itemStack) {
        // Find the matching collection item
        for (ItemCategory category : this.categories.values()) {
            for (CollectionItem item : category.getAllItems()) {
                if (item.matches(itemStack)) {
                    final boolean added = this.addToCollection(player.getUniqueId(), 
                            item.getCategoryId(), item.getId());
                    
                    if (added) {
                        this.notifyCollection(player, item);
                    }
                    
                    return added;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Add an item to a player's collection
     * 
     * @param playerId The UUID of the player
     * @param categoryId The category ID
     * @param itemId The item ID
     * @return True if the item was newly added to the collection
     */
    public boolean addToCollection(final UUID playerId, final String categoryId, final String itemId) {
        final PlayerCollection collection = this.getPlayerCollection(playerId);
        return collection.addItem(categoryId, itemId);
    }
    
    /**
     * Notify a player that they've collected a new item
     * 
     * @param player The player
     * @param item The collected item
     */
    private void notifyCollection(final Player player, final CollectionItem item) {
        if (!this.plugin.getConfigManager().getMainConfig().getBoolean("settings.collection-notifications", true)) {
            return;
        }
        
        // Get the clean item name
        final String itemName = item.getName().replace('&', '§');
        
        // Send title notification
        if (this.plugin.getConfigManager().getMainConfig().getBoolean("settings.use-titles", true)) {
            player.sendTitle(
                    "§aItem Collected!", 
                    itemName, 
                    10, 40, 10
            );
        } else {
            player.sendMessage("§a[Collection Log] §fYou've collected: " + itemName);
        }
        
        // Play sound
        if (this.plugin.getConfigManager().getMainConfig().getBoolean("settings.play-sound", true)) {
            final String soundName = this.plugin.getConfigManager().getMainConfig()
                    .getString("settings.collection-sound", "ENTITY_PLAYER_LEVELUP");
            
            try {
                final Sound sound = Sound.valueOf(soundName);
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                this.plugin.getLogger().warning("Invalid sound name in config: " + soundName);
            }
        }
        
        // Show particles
        if (this.plugin.getConfigManager().getMainConfig().getBoolean("settings.show-particles", true)) {
            final String particleName = this.plugin.getConfigManager().getMainConfig()
                    .getString("settings.particle-type", "VILLAGER_HAPPY");
            
            try {
                final Particle particle = Particle.valueOf(particleName);
                final Location loc = player.getLocation().add(0, 1, 0);
                
                player.getWorld().spawnParticle(particle, loc, 20, 0.5, 0.5, 0.5, 0.1);
            } catch (IllegalArgumentException e) {
                this.plugin.getLogger().warning("Invalid particle name in config: " + particleName);
            }
        }
    }
    
    /**
     * Clear the collection data for a player
     * 
     * @param playerId The UUID of the player
     */
    public void clearPlayerData(final UUID playerId) {
        final File playerFile = this.getPlayerDataFile(playerId);
        if (playerFile.exists() && !playerFile.delete()) {
            this.plugin.getLogger().warning("Failed to delete player data file: " + playerFile.getPath());
        }
        
        this.playerCollections.remove(playerId);
        this.plugin.debug("Cleared collection data for player: " + playerId);
    }
}
