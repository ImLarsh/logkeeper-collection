
package com.collectionlog;

import com.collectionlog.commands.CollectionLogCommand;
import com.collectionlog.config.ConfigManager;
import com.collectionlog.listeners.CollectionListener;
import com.collectionlog.listeners.PlayerListener;
import com.collectionlog.manager.CollectionManager;
import com.collectionlog.manager.GuiManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class CollectionLog extends JavaPlugin {

    @Getter
    private static CollectionLog instance;
    
    @Getter
    private ConfigManager configManager;
    
    @Getter
    private CollectionManager collectionManager;
    
    @Getter
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        // Set the instance
        instance = this;
        
        this.getLogger().info("Initializing Collection Log plugin...");
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigurations();
        
        this.collectionManager = new CollectionManager(this);
        this.guiManager = new GuiManager(this);
        
        // Register commands
        this.getCommand("log").setExecutor(new CollectionLogCommand(this));
        
        // Register listeners
        this.getServer().getPluginManager().registerEvents(new CollectionListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Schedule regular data saving
        int saveInterval = this.configManager.getMainConfig().getInt("settings.save-interval", 10);
        if (saveInterval > 0) {
            this.getServer().getScheduler().runTaskTimerAsynchronously(
                this,
                () -> this.collectionManager.saveAllData(),
                saveInterval * 1200L, // Convert minutes to ticks (20 ticks/second * 60 seconds)
                saveInterval * 1200L
            );
        }
        
        this.getLogger().info("Collection Log plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (this.collectionManager != null) {
            this.collectionManager.saveAllData();
        }
        
        this.getLogger().info("Collection Log plugin disabled successfully!");
    }
    
    /**
     * Log a debug message if debug mode is enabled
     * 
     * @param message The message to log
     */
    public void debug(String message) {
        if (this.configManager != null && this.configManager.getMainConfig().getBoolean("debug", false)) {
            this.getLogger().log(Level.INFO, "[Debug] " + message);
        }
    }
}
