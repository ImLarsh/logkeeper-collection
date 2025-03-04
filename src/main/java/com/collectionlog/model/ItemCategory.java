
package com.collectionlog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCategory {

    private String id;
    private Material displayItem;
    private String displayName;
    private Map<String, CollectionItem> items;
    
    /**
     * Create an item category from a configuration section
     * 
     * @param id The unique identifier for this category
     * @param section The configuration section containing category details
     * @return A new ItemCategory
     */
    public static ItemCategory fromConfig(final String id, final ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        
        final Material displayItem;
        try {
            displayItem = Material.valueOf(section.getString("display-item", "BOOK").toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
        
        final ItemCategory category = ItemCategory.builder()
                .id(id)
                .displayItem(displayItem)
                .displayName(section.getString("display-name", id))
                .items(new HashMap<>())
                .build();
        
        // Load the items
        final ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String itemId : itemsSection.getKeys(false)) {
                final ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemId);
                if (itemSection != null) {
                    final CollectionItem item = CollectionItem.fromConfig(itemId, id, itemSection);
                    if (item != null) {
                        category.getItems().put(itemId, item);
                    }
                }
            }
        }
        
        return category;
    }
    
    /**
     * Convert this category to an ItemStack for display in the GUI
     * 
     * @param collectedCount The number of items collected in this category
     * @return An ItemStack representing this category
     */
    public ItemStack toItemStack(final int collectedCount) {
        final ItemStack item = new ItemStack(this.displayItem);
        final ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(this.displayName.replace('&', '§'));
            
            final List<String> lore = new ArrayList<>();
            lore.add("§7" + collectedCount + "/" + this.items.size() + " Items Collected");
            
            int percentage = this.items.isEmpty() ? 0 : (collectedCount * 100) / this.items.size();
            lore.add("§7" + percentage + "% Complete");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Get all items in this category
     * 
     * @return A list of all items in this category
     */
    public List<CollectionItem> getAllItems() {
        return new ArrayList<>(this.items.values());
    }
}
