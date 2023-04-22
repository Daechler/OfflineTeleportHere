package net.daechler.offlineteleporthere;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OfflineTeleportHere extends JavaPlugin implements CommandExecutor {
    // Map to store teleport requests for players who are not currently online
    private final Map<UUID, Location> teleportRequests = new HashMap<>();

    @Override
    public void onEnable() {
        // Register the command executor and event listener
        getCommand("offlineteleporthere").setExecutor(this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("offlineteleporthere") && sender.hasPermission("offlineteleporthere.use")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /offlineteleporthere <player>");
                return true;
            }
            // Get the target player, even if they are offline
            Player target = Bukkit.getPlayerExact(args[0]);
            UUID targetId = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(args[0]).getUniqueId();

            // Get the location of the admin who sent the teleport request
            Location location = sender instanceof Player ? ((Player) sender).getLocation() : null;
            if (location != null) {
                // Record the teleport request in the map
                teleportRequests.put(targetId, location);
                sender.sendMessage(ChatColor.GREEN + args[0] + " will be teleported to this location as soon as they enter the server.");
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
            }
            return true;
        }
        return false;
    }

    private class PlayerJoinListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            if (teleportRequests.containsKey(playerId)) {
                // If there is a teleport request for the player, teleport them to the recorded location
                Location location = teleportRequests.get(playerId);
                if (location != null) {
                    player.teleport(location);
                }
                // Remove the teleport request from the map
                teleportRequests.remove(playerId);
            }
        }
    }
}
