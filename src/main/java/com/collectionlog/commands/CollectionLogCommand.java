
package com.collectionlog.commands;

import com.collectionlog.CollectionLog;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionLogCommand implements CommandExecutor, TabCompleter {

    private final CollectionLog plugin;
    
    public CollectionLogCommand(final CollectionLog plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        
        if (args.length == 0) {
            // Open the main collection log GUI
            this.plugin.getGuiManager().openMainMenu(player);
            return true;
        }
        
        // Admin commands
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("collectionlog.admin")) {
                this.plugin.getConfigManager().reloadConfigurations();
                this.plugin.getCollectionManager().loadCategories();
                sender.sendMessage("§aCollection Log configuration reloaded.");
                return true;
            }
            
            if (args[0].equalsIgnoreCase("reset") && sender.hasPermission("collectionlog.admin")) {
                if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("confirm")) {
                        this.plugin.getCollectionManager().clearPlayerData(player.getUniqueId());
                        sender.sendMessage("§aYour collection data has been reset.");
                        return true;
                    }
                }
                
                sender.sendMessage("§cWarning: This will reset all your collection data.");
                sender.sendMessage("§cUse §f/log reset confirm§c to proceed.");
                return true;
            }
            
            if (args[0].equalsIgnoreCase("category") && args.length >= 2) {
                final String categoryId = args[1].toLowerCase();
                if (this.plugin.getCollectionManager().getCategories().containsKey(categoryId)) {
                    this.plugin.getGuiManager().openCategoryMenu(player, categoryId);
                    return true;
                } else {
                    sender.sendMessage("§cCategory not found.");
                    return true;
                }
            }
            
            // Show help
            sender.sendMessage("§6Collection Log Commands:");
            sender.sendMessage("§f/log §7- Open the collection log GUI");
            
            if (sender.hasPermission("collectionlog.admin")) {
                sender.sendMessage("§f/log category <id> §7- Open a specific category");
                sender.sendMessage("§f/log reload §7- Reload the plugin configuration");
                sender.sendMessage("§f/log reset §7- Reset your collection data");
            }
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, 
            final String alias, final String[] args) {
        
        final List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            final List<String> commands = new ArrayList<>(Arrays.asList("category"));
            
            if (sender.hasPermission("collectionlog.admin")) {
                commands.add("reload");
                commands.add("reset");
            }
            
            final String input = args[0].toLowerCase();
            completions.addAll(commands.stream()
                    .filter(cmd -> cmd.startsWith(input))
                    .collect(Collectors.toList()));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("category")) {
                final String input = args[1].toLowerCase();
                completions.addAll(this.plugin.getCollectionManager().getCategories().keySet().stream()
                        .filter(cat -> cat.startsWith(input))
                        .collect(Collectors.toList()));
            } else if (args[0].equalsIgnoreCase("reset") && sender.hasPermission("collectionlog.admin")) {
                completions.add("confirm");
            }
        }
        
        return completions;
    }
}
