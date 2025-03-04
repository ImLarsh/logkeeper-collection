
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionItem {

    private String id;
    private String categoryId;
    private Material material;
    private String name;
    private Integer customModelData;
    private List<String> lore;
    
    /**
     * Create a collection item from a configuration section
     * 
     * @param id The unique identifier for this item
     * @param categoryId The category this item belongs to
     * @param section The configuration section containing item details
     * @return A new CollectionItem
     */
    public static CollectionItem fromConfig(final String id, final String categoryId, final ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        
        final Material material;
        try {
            material = Material.valueOf(section.getString("material", "STONE").toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
        
        return CollectionItem.builder()
                .id(id)
                .categoryId(categoryId)
                .material(material)
                .name(section.getString("name", material.name()))
                .customModelData(section.isSet("model-data") ? section.getInt("model-data") : null)
                .lore(section.getStringList("lore"))
                .build();
    }
    
    /**
     * Convert this collection item to an ItemStack
     * 
     * @param collected Whether the player has collected this item
     * @param config The GUI configuration
     * @return An ItemStack representing this collection item
     */
    public ItemStack toItemStack(final boolean collected, final ConfigurationSection config) {
        if (!collected && config != null && config.isSet("uncollected.material")) {
            // Return a placeholder item for uncollected items
            final Material uncollectedMaterial = Material.valueOf(
                    config.getString("uncollected.material", "GRAY_DYE").toUpperCase());
            final ItemStack item = new ItemStack(uncollectedMaterial);
            final ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                meta.setDisplayName(config.getString("uncollected.name", "???")
                        .replace('&', '§'));
                
                final List<String> uncollectedLore = config.getStringList("uncollected.lore").stream()
                        .map(line -> line.replace('&', '§'))
                        .collect(Collectors.toList());
                
                meta.setLore(uncollectedLore);
                item.setItemMeta(meta);
            }
            
            return item;
        }
        
        // Create the actual item
        final ItemStack item = new ItemStack(this.material);
        final ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(this.name.replace('&', '§'));
            
            final List<String> itemLore = new ArrayList<>();
            
            // Add the original lore
            if (this.lore != null && !this.lore.isEmpty()) {
                itemLore.addAll(this.lore.stream()
                        .map(line -> line.replace('&', '§'))
                        .collect(Collectors.toList()));
            }
            
            // Add collected status if collected
            if (collected && config != null && config.isSet("collected.lore-addition")) {
                final List<String> collectedLore = config.getStringList("collected.lore-addition").stream()
                        .map(line -> line.replace('&', '§'))
                        .collect(Collectors.toList());
                
                itemLore.addAll(collectedLore);
            }
            
            meta.setLore(itemLore);
            
            // Set custom model data if specified
            if (this.customModelData != null) {
                meta.setCustomModelData(this.customModelData);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Check if an ItemStack matches this collection item
     * 
     * @param itemStack The ItemStack to check
     * @return True if the ItemStack matches this collection item
     */
    public boolean matches(final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() != this.material) {
            return false;
        }
        
        final ItemMeta meta = itemStack.getItemMeta();
        
        // Check custom model data if specified
        if (this.customModelData != null) {
            if (meta == null || !meta.hasCustomModelData() || meta.getCustomModelData() != this.customModelData) {
                return false;
            }
        }
        
        // In a real implementation, we might want to check for item name and lore,
        // but this depends on the specific needs of the plugin.
        // For simplicity, we'll just check the material and custom model data.
        
        return true;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectionItem that = (CollectionItem) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(categoryId, that.categoryId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, categoryId);
    }
}
