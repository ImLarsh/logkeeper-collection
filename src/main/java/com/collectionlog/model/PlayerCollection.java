
package com.collectionlog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerCollection {

    private UUID playerId;
    private Map<String, Set<String>> collectedItems;
    
    /**
     * Initialize a new empty player collection
     * 
     * @param playerId The UUID of the player
     * @return A new PlayerCollection
     */
    public static PlayerCollection createEmpty(final UUID playerId) {
        return PlayerCollection.builder()
                .playerId(playerId)
                .collectedItems(new HashMap<>())
                .build();
    }
    
    /**
     * Load a player collection from a file
     * 
     * @param playerId The UUID of the player
     * @param file The file to load from
     * @return The loaded PlayerCollection
     */
    public static PlayerCollection fromFile(final UUID playerId, final File file) {
        if (!file.exists()) {
            return createEmpty(playerId);
        }
        
        final YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        final PlayerCollection collection = createEmpty(playerId);
        
        final ConfigurationSection categoriesSection = config.getConfigurationSection("collected-items");
        if (categoriesSection != null) {
            for (String categoryId : categoriesSection.getKeys(false)) {
                final Set<String> itemIds = new HashSet<>(
                        categoriesSection.getStringList(categoryId));
                
                collection.collectedItems.put(categoryId, itemIds);
            }
        }
        
        return collection;
    }
    
    /**
     * Save this player collection to a file
     * 
     * @param file The file to save to
     * @throws IOException If an I/O error occurs
     */
    public void saveToFile(final File file) throws IOException {
        final YamlConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<String, Set<String>> entry : this.collectedItems.entrySet()) {
            config.set("collected-items." + entry.getKey(), 
                    entry.getValue().stream().collect(java.util.stream.Collectors.toList()));
        }
        
        config.save(file);
    }
    
    /**
     * Check if a player has collected an item
     * 
     * @param categoryId The category ID of the item
     * @param itemId The ID of the item
     * @return True if the player has collected the item
     */
    public boolean hasCollected(final String categoryId, final String itemId) {
        final Set<String> categoryItems = this.collectedItems.getOrDefault(categoryId, new HashSet<>());
        return categoryItems.contains(itemId);
    }
    
    /**
     * Add an item to the player's collection
     * 
     * @param categoryId The category ID of the item
     * @param itemId The ID of the item
     * @return True if the item was newly added, false if already collected
     */
    public boolean addItem(final String categoryId, final String itemId) {
        final Set<String> categoryItems = this.collectedItems.computeIfAbsent(
                categoryId, k -> new HashSet<>());
        
        return categoryItems.add(itemId);
    }
    
    /**
     * Get the number of items collected in a category
     * 
     * @param categoryId The category ID
     * @return The number of collected items
     */
    public int getCategoryCollectionCount(final String categoryId) {
        final Set<String> categoryItems = this.collectedItems.getOrDefault(categoryId, new HashSet<>());
        return categoryItems.size();
    }
    
    /**
     * Get the total number of items collected across all categories
     * 
     * @return The total number of collected items
     */
    public int getTotalCollectionCount() {
        return this.collectedItems.values().stream()
                .mapToInt(Set::size)
                .sum();
    }
}
