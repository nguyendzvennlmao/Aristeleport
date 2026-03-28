package me.aris.aristeleport.listeners;

import me.aris.aristeleport.ArisTeleport;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeleportListener implements Listener {
    private final ArisTeleport plugin;
    public final Map<UUID, Object> tasks = new HashMap<>();
    private final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public TeleportListener(ArisTeleport plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (tasks.containsKey(p.getUniqueId())) {
            if (e.getFrom().distance(e.getTo()) > 0.1) {
                cancelTask(p.getUniqueId());
                p.sendMessage(color(plugin.getConfig().getString("messages.global.cancelled")));
                p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.cancel")), 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cancelTask(e.getPlayer().getUniqueId());
    }

    private void cancelTask(UUID uuid) {
        Object task = tasks.remove(uuid);
        if (task instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask) {
            ((io.papermc.paper.threadedregions.scheduler.ScheduledTask) task).cancel();
        }
    }

    public void startTeleport(Player p, Location loc, String type, String warpName) {
        if (tasks.containsKey(p.getUniqueId()) || loc == null) return;
        
        int delay = plugin.getConfig().getInt("teleport-delay");
        final int[] remaining = {delay};

        Object scheduledTask = Bukkit.getRegionScheduler().runAtFixedRate(plugin, p.getLocation(), (task) -> {
            if (!p.isOnline()) {
                task.cancel();
                tasks.remove(p.getUniqueId());
                return;
            }

            if (remaining[0] > 0) {
                String raw = plugin.getConfig().getString("messages." + type + ".countdown")
                        .replace("%name%", warpName).replace("%time%", String.valueOf(remaining[0]));
                String colored = color(raw);
                
                if (plugin.getConfig().getBoolean("settings.use-chat")) p.sendMessage(colored);
                if (plugin.getConfig().getBoolean("settings.use-actionbar")) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(colored));
                }
                
                p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.countdown")), 1.0f, 1.0f);
                remaining[0]--;
            } else {
                task.cancel();
                tasks.remove(p.getUniqueId());
                
                p.getScheduler().run(plugin, (st) -> {
                    p.teleport(loc);
                    String rawSuccess = plugin.getConfig().getString("messages." + type + ".success").replace("%name%", warpName);
                    String coloredSuccess = color(rawSuccess);
                    
                    if (plugin.getConfig().getBoolean("settings.use-chat")) p.sendMessage(coloredSuccess);
                    if (plugin.getConfig().getBoolean("settings.use-actionbar")) {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(coloredSuccess));
                    }
                    
                    p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.success")), 1.0f, 1.0f);
                }, null);
            }
        }, 1L, 20L);

        tasks.put(p.getUniqueId(), scheduledTask);
    }

    public String color(String message) {
        if (message == null) return "";
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString());
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }
    }
