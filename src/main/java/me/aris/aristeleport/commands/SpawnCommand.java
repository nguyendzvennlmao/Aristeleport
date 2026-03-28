package me.aris.aristeleport.commands;

import me.aris.aristeleport.ArisTeleport;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.*;

public class SpawnCommand implements CommandExecutor {
    private final ArisTeleport plugin;
    public SpawnCommand(ArisTeleport plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (label.equalsIgnoreCase("setspawn")) {
            if (!p.hasPermission("aristeleport.admin")) {
                p.sendMessage(plugin.getTeleportListener().color(plugin.getConfig().getString("messages.global.no-permission")));
                return true;
            }
            if (args.length == 0) return false;
            plugin.getLocationManager().setLocation("spawn." + args[0], p.getLocation());
            p.sendMessage(plugin.getTeleportListener().color(plugin.getConfig().getString("messages.spawn.set").replace("%name%", args[0])));
            return true;
        }

        if (label.equalsIgnoreCase("delspawn")) {
            if (!p.hasPermission("aristeleport.admin")) {
                p.sendMessage(plugin.getTeleportListener().color(plugin.getConfig().getString("messages.global.no-permission")));
                return true;
            }
            if (args.length == 0) return false;
            plugin.getLocationManager().removeLocation("spawn." + args[0]);
            p.sendMessage(plugin.getTeleportListener().color(plugin.getConfig().getString("messages.spawn.delete").replace("%name%", args[0])));
            return true;
        }

        var section = plugin.getLocationManager().getConfig().getConfigurationSection("spawn");
        if (section == null || section.getKeys(false).isEmpty()) {
            p.sendMessage(plugin.getTeleportListener().color(plugin.getConfig().getString("messages.spawn.not-set")));
            return true;
        }

        List<String> keys = new ArrayList<>(section.getKeys(false));
        String randomKey = keys.get(new Random().nextInt(keys.size()));
        Location loc = plugin.getLocationManager().getLocation("spawn." + randomKey);
        
        if (loc != null) {
            plugin.getTeleportListener().startTeleport(p, loc, "spawn", "Spawn");
        }
        return true;
    }
        }
