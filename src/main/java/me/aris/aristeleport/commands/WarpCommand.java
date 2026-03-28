package me.aris.aristeleport.commands;

import me.aris.aristeleport.ArisTeleport;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.*;

public class WarpCommand implements CommandExecutor, TabCompleter {
    private final ArisTeleport plugin;
    public WarpCommand(ArisTeleport plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (label.equalsIgnoreCase("setwarp")) {
            if (!p.hasPermission("aristeleport.admin")) {
                p.sendMessage(plugin.getTeleportListener().color(plugin.getConfig().getString("messages.global.no-permission")));
                return true;
            }
            if (args.length == 0) return false;
            plugin.getLocationManager().setLocation("warps." + args[0], p.getLocation());
            p.sendMessage(plugin.getTeleportListener().color(plugin.getConfig().getString("messages.warp.set").replace("%name%", args[0])));
            return true;
        }

        if (label.equalsIgnoreCase("delwarp")) {
            if (!p.hasPermission("aristeleport.admin")) {
                p.sendMessage(plugin.getTeleportListener().color(plugin.getConfig().getString("messages.global.no-permission")));
                return true;
            }
            if (args.length == 0) return false;
            plugin.getLocationManager().removeLocation("warps." + args[0]);
            p.sendMessage(plugin.getTeleportListener().color(plugin.getConfig().getString("messages.warp.delete").replace("%name%", args[0])));
            return true;
        }

        if (args.length == 0) {
            var section = plugin.getLocationManager().getConfig().getConfigurationSection("warps");
            String list = (section == null) ? "" : String.join(", ", section.getKeys(false));
            p.sendMessage(plugin.getTeleportListener().color(plugin.getConfig().getString("messages.warp.usage").replace("%list%", list)));
            return true;
        }

        Location loc = plugin.getLocationManager().getLocation("warps." + args[0]);
        if (loc == null) {
            p.sendMessage(plugin.getTeleportListener().color(plugin.getConfig().getString("messages.warp.not-set").replace("%name%", args[0])));
            return true;
        }

        plugin.getTeleportListener().startTeleport(p, loc, "warp", args[0]);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            var section = plugin.getLocationManager().getConfig().getConfigurationSection("warps");
            if (section != null) {
                List<String> suggestions = new ArrayList<>();
                String input = args[0].toLowerCase();
                for (String key : section.getKeys(false)) {
                    if (key.toLowerCase().startsWith(input)) suggestions.add(key);
                }
                return suggestions;
            }
        }
        return Collections.emptyList();
    }
        }
