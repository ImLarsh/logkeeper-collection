
package com.collectionlog.config;

import com.collectionlog.CollectionLog;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigManager {

    private final CollectionLog plugin;
    
    @Getter
    private FileConfiguration mainConfig;
    
    @Getter
    private FileConfiguration itemsConfig;
    
    @Getter
    private FileConfiguration guiConfig;
    
    private File mainConfigFile;
    private File itemsConfigFile;
    private File guiConfigFile;

    public ConfigManager(final CollectionLog plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Load all configuration files
     */
    public void loadConfigurations() {
        this.loadMainConfig();
        this.loadItemsConfig();
        this.loadGuiConfig();
    }
    
    /**
     * Save all configuration files
     */
    public void saveConfigurations() {
        try {
            this.mainConfig.save(this.mainConfigFile);
            this.itemsConfig.save(this.itemsConfigFile);
            this.guiConfig.save(this.guiConfigFile);
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config files", e);
        }
    }
    
    /**
     * Load the main config (config.yml)
     */
    public void loadMainConfig() {
        this.mainConfigFile = new File(this.plugin.getDataFolder(), "config.yml");
        
        if (!this.mainConfigFile.exists()) {
            this.plugin.saveResource("config.yml", false);
        }
        
        this.mainConfig = YamlConfiguration.loadConfiguration(this.mainConfigFile);
        this.plugin.debug("Main configuration loaded");
    }
    
    /**
     * Load the items config (items.yml)
     */
    public void loadItemsConfig() {
        this.itemsConfigFile = new File(this.plugin.getDataFolder(), "items.yml");
        
        if (!this.itemsConfigFile.exists()) {
            this.plugin.saveResource("items.yml", false);
        }
        
        this.itemsConfig = YamlConfiguration.loadConfiguration(this.itemsConfigFile);
        this.plugin.debug("Items configuration loaded");
    }
    
    /**
     * Load the GUI config (gui.yml)
     */
    public void loadGuiConfig() {
        this.guiConfigFile = new File(this.plugin.getDataFolder(), "gui.yml");
        
        if (!this.guiConfigFile.exists()) {
            this.plugin.saveResource("gui.yml", false);
        }
        
        this.guiConfig = YamlConfiguration.loadConfiguration(this.guiConfigFile);
        this.plugin.debug("GUI configuration loaded");
    }
    
    /**
     * Reload all configuration files
     */
    public void reloadConfigurations() {
        this.mainConfig = YamlConfiguration.loadConfiguration(this.mainConfigFile);
        this.itemsConfig = YamlConfiguration.loadConfiguration(this.itemsConfigFile);
        this.guiConfig = YamlConfiguration.loadConfiguration(this.guiConfigFile);
        
        this.plugin.debug("All configurations reloaded");
    }
}
