package pl.szczerbal.voidcleaner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VoidCommand implements CommandExecutor, TabCompleter {
    private final VoidCleaner plugin;
    
    public VoidCommand(VoidCleaner plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                return true;
            }
            
            if (!player.hasPermission("voidcleaner.use")) {
                String message = plugin.getPluginConfig().getMessage("no-permission");
                player.sendMessage(message);
                return true;
            }
            
            plugin.getVoidGUI().openInventory(player);
            return true;
        }
        
        // Admin commands
        if (!sender.hasPermission("voidcleaner.admin")) {
            String message = plugin.getPluginConfig().getMessage("no-permission");
            sender.sendMessage(message);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "clean":
                // cleanupItems() broadcastuje cleanup-complete tylko gdy cos usunieto.
                // void-manual-cleanup informuje kto wywolal – zawsze wysylamy.
                plugin.getItemCleaner().cleanupItems();
                Bukkit.broadcastMessage(
                    plugin.getPluginConfig().getMessage("void-manual-cleanup", "%player%", sender.getName()));
                break;

            case "open":
                if (plugin.getVoidGUI().isOpen()) {
                    sender.sendMessage(plugin.getPluginConfig().getMessage("void-already-open"));
                    return true;
                }
                plugin.getVoidGUI().openVoid();
                Bukkit.broadcastMessage(
                    plugin.getPluginConfig().getMessage("void-manual-open", "%player%", sender.getName()));
                break;

            case "close":
                if (!plugin.getVoidGUI().isOpen()) {
                    sender.sendMessage(plugin.getPluginConfig().getMessage("void-already-closed"));
                    return true;
                }
                plugin.getVoidGUI().closeVoid();
                Bukkit.broadcastMessage(
                    plugin.getPluginConfig().getMessage("void-manual-close", "%player%", sender.getName()));
                break;

            case "fullclean":
                // fullCleanup() wewnetrznie broadcastuje cleanup-complete.
                // void-manual-fullclean to osobna wiadomosc konfigurowana w config.yml.
                plugin.getItemCleaner().fullCleanup();
                Bukkit.broadcastMessage(
                    plugin.getPluginConfig().getMessage("void-manual-fullclean", "%player%", sender.getName()));
                break;

            case "worldclean":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                    return true;
                }
                sender.sendMessage(ChatColor.YELLOW + "Starting async world cleanup (this may take a while)...");
                plugin.getItemCleaner().startWorldCleanup(player.getWorld(), sender);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Usage: /void [clean|open|close|fullclean|worldclean]");
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("voidcleaner.admin")) {
            return Arrays.asList("clean", "open", "close", "fullclean", "worldclean")
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Arrays.asList();
    }
}
