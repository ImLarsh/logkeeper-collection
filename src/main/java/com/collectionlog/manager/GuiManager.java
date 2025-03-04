
package com.collectionlog.manager;

import com.collectionlog.CollectionLog;
import com.collectionlog.model.CollectionItem;
import com.collectionlog.model.ItemCategory;
import com.collectionlog.model.PlayerCollection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GuiManager {

    private final CollectionLog plugin;
    private final Map<UUID, String> openCategories;
    
    public GuiManager(final CollectionLog plugin) {
        this.plugin = plugin;
        this.openCategories = new HashMap<>();
    }
    
    /**
     * Open the main collection log GUI for a player
     * 
     * @param player The player to open the GUI for
     */
    public void openMainMenu(final Player player) {
        // Clear the open category for this player
        this.openCategories.remove(player.getUniqueId());
        
        final ConfigurationSection guiConfig = this.plugin.getConfigManager()
                .getGuiConfig().getConfigurationSection("main-menu");
        
        if (guiConfig == null) {
            player.sendMessage("§cError: GUI configuration is invalid.");
            return;
        }
        
        final String title = guiConfig.getString("title", "Collection Log")
                .replace('&', '§');
        final int size = guiConfig.getInt("size", 54);
        
        final Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Add border items
        if (guiConfig.isSet("border-item.material")) {
            final ItemStack borderItem = this.createGuiItem(guiConfig.getConfigurationSection("border-item"));
            
            // Add border around the edge of the inventory
            for (int i = 0; i < size; i++) {
                if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                    inventory.setItem(i, borderItem);
                }
            }
        }
        
        // Add info item
        if (guiConfig.isSet("info.material")) {
            final ItemStack infoItem = this.createGuiItem(guiConfig.getConfigurationSection("info"));
            final int infoSlot = guiConfig.getInt("info.slot", 4);
            inventory.setItem(infoSlot, infoItem);
        }
        
        // Add close button
        if (guiConfig.isSet("close.material")) {
            final ItemStack closeItem = this.createGuiItem(guiConfig.getConfigurationSection("close"));
            final int closeSlot = guiConfig.getInt("close.slot", 49);
            inventory.setItem(closeSlot, closeItem);
        }
        
        // Add category items
        final List<Integer> categorySlots = guiConfig.getIntegerList("category-slots");
        if (categorySlots.isEmpty()) {
            this.plugin.getLogger().warning("No category slots defined in GUI configuration");
            return;
        }
        
        final PlayerCollection playerCollection = this.plugin.getCollectionManager()
                .getPlayerCollection(player.getUniqueId());
        
        int index = 0;
        for (ItemCategory category : this.plugin.getCollectionManager().getCategories().values()) {
            if (index >= categorySlots.size()) {
                break;
            }
            
            final int collectedCount = playerCollection.getCategoryCollectionCount(category.getId());
            final ItemStack categoryItem = category.toItemStack(collectedCount);
            inventory.setItem(categorySlots.get(index), categoryItem);
            
            index++;
        }
        
        player.openInventory(inventory);
    }
    
    /**
     * Open a category GUI for a player
     * 
     * @param player The player to open the GUI for
     * @param categoryId The category ID to open
     */
    public void openCategoryMenu(final Player player, final String categoryId) {
        // Store the open category for this player
        this.openCategories.put(player.getUniqueId(), categoryId);
        
        final ItemCategory category = this.plugin.getCollectionManager().getCategories().get(categoryId);
        if (category == null) {
            player.sendMessage("§cError: Category not found.");
            return;
        }
        
        final ConfigurationSection guiConfig = this.plugin.getConfigManager()
                .getGuiConfig().getConfigurationSection("category-menu");
        
        if (guiConfig == null) {
            player.sendMessage("§cError: GUI configuration is invalid.");
            return;
        }
        
        final String title = guiConfig.getString("title", "%category% Collection")
                .replace("%category%", category.getDisplayName().replace('&', '§'))
                .replace('&', '§');
        final int size = guiConfig.getInt("size", 54);
        
        final Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Add border items (reusing code from openMainMenu)
        if (this.plugin.getConfigManager().getGuiConfig().isSet("main-menu.border-item.material")) {
            final ItemStack borderItem = this.createGuiItem(
                    this.plugin.getConfigManager().getGuiConfig().getConfigurationSection("main-menu.border-item"));
            
            // Add border around the edge of the inventory
            for (int i = 0; i < size; i++) {
                if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                    inventory.setItem(i, borderItem);
                }
            }
        }
        
        // Add progress indicator
        if (guiConfig.isSet("progress.material")) {
            final PlayerCollection playerCollection = this.plugin.getCollectionManager()
                    .getPlayerCollection(player.getUniqueId());
            
            final int collectedCount = playerCollection.getCategoryCollectionCount(categoryId);
            final int totalItems = category.getItems().size();
            final int percentage = totalItems > 0 ? (collectedCount * 100) / totalItems : 0;
            
            final ItemStack progressItem = this.createGuiItem(guiConfig.getConfigurationSection("progress"));
            final ItemMeta meta = progressItem.getItemMeta();
            
            if (meta != null) {
                // Update lore with actual progress
                final List<String> lore = meta.getLore();
                if (lore != null) {
                    final List<String> updatedLore = lore.stream()
                            .map(line -> line
                                    .replace("%collected%", String.valueOf(collectedCount))
                                    .replace("%total%", String.valueOf(totalItems))
                                    .replace("%percentage%", String.valueOf(percentage)))
                            .collect(Collectors.toList());
                    
                    meta.setLore(updatedLore);
                }
                
                progressItem.setItemMeta(meta);
            }
            
            final int progressSlot = guiConfig.getInt("progress.slot", 4);
            inventory.setItem(progressSlot, progressItem);
        }
        
        // Add back button
        if (guiConfig.isSet("back.material")) {
            final ItemStack backItem = this.createGuiItem(guiConfig.getConfigurationSection("back"));
            final int backSlot = guiConfig.getInt("back.slot", 49);
            inventory.setItem(backSlot, backItem);
        }
        
        // Add items from the category
        final List<Integer> itemSlots = guiConfig.getIntegerList("item-slots");
        if (itemSlots.isEmpty()) {
            this.plugin.getLogger().warning("No item slots defined in GUI configuration");
            return;
        }
        
        final ConfigurationSection collectableConfig = guiConfig.getConfigurationSection("collected");
        final PlayerCollection playerCollection = this.plugin.getCollectionManager()
                .getPlayerCollection(player.getUniqueId());
        
        int index = 0;
        for (CollectionItem item : category.getAllItems()) {
            if (index >= itemSlots.size()) {
                break;
            }
            
            final boolean collected = playerCollection.hasCollected(categoryId, item.getId());
            final ItemStack itemStack = item.toItemStack(collected, guiConfig);
            
            // Add glow effect if collected and configured
            if (collected && collectableConfig != null && collectableConfig.getBoolean("glow", false)) {
                final ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                    itemStack.setItemMeta(meta);
                }
            }
            
            inventory.setItem(itemSlots.get(index), itemStack);
            
            index++;
        }
        
        player.openInventory(inventory);
    }
    
    /**
     * Handle a click in the collection log GUI
     * 
     * @param player The player who clicked
     * @param inventory The inventory that was clicked
     * @param slot The slot that was clicked
     * @return True if the click was handled
     */
    public boolean handleClick(final Player player, final Inventory inventory, final int slot) {
        final String openCategory = this.openCategories.get(player.getUniqueId());
        
        if (openCategory == null) {
            // Main menu
            final ConfigurationSection guiConfig = this.plugin.getConfigManager()
                    .getGuiConfig().getConfigurationSection("main-menu");
            
            if (guiConfig == null) {
                return false;
            }
            
            // Check for close button
            final int closeSlot = guiConfig.getInt("close.slot", 49);
            if (slot == closeSlot) {
                player.closeInventory();
                return true;
            }
            
            // Check for category slots
            final List<Integer> categorySlots = guiConfig.getIntegerList("category-slots");
            if (categorySlots.contains(slot)) {
                final int index = categorySlots.indexOf(slot);
                final List<String> categories = new ArrayList<>(
                        this.plugin.getCollectionManager().getCategories().keySet());
                
                if (index < categories.size()) {
                    final String categoryId = categories.get(index);
                    this.openCategoryMenu(player, categoryId);
                    return true;
                }
            }
        } else {
            // Category menu
            final ConfigurationSection guiConfig = this.plugin.getConfigManager()
                    .getGuiConfig().getConfigurationSection("category-menu");
            
            if (guiConfig == null) {
                return false;
            }
            
            // Check for back button
            final int backSlot = guiConfig.getInt("back.slot", 49);
            if (slot == backSlot) {
                this.openMainMenu(player);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Create an ItemStack for the GUI
     * 
     * @param config The configuration section for the item
     * @return The created ItemStack
     */
    private ItemStack createGuiItem(final ConfigurationSection config) {
        if (config == null) {
            return new ItemStack(Material.STONE);
        }
        
        final Material material;
        try {
            material = Material.valueOf(config.getString("material", "STONE").toUpperCase());
        } catch (IllegalArgumentException e) {
            this.plugin.getLogger().warning("Invalid material in GUI configuration: " + 
                    config.getString("material", "STONE"));
            return new ItemStack(Material.STONE);
        }
        
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            final String name = config.getString("name");
            if (name != null) {
                meta.setDisplayName(name.replace('&', '§'));
            }
            
            final List<String> lore = config.getStringList("lore");
            if (!lore.isEmpty()) {
                meta.setLore(lore.stream()
                        .map(line -> line.replace('&', '§'))
                        .collect(Collectors.toList()));
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
}
