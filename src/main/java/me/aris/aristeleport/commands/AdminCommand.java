package me.aris.aristeleport.commands;

import me.aris.aristeleport.ArisTeleport;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AdminCommand implements CommandExecutor {
    private final ArisTeleport plugin;
    public AdminCommand(ArisTeleport plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("aristeleport.admin")) {
            sender.sendMessage(plugin.getTeleportListener().color(plugin.getConfig().getString("messages.global.no-permission")));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(plugin.getTeleportListener().color(plugin.getConfig().getString("messages.global.reload")));
            return true;
        }
        for (String line : plugin.getConfig().getStringList("messages.global.help")) {
            sender.sendMessage(plugin.getTeleportListener().color(line));
        }
        return true;
    }
            }
