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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeleportListener implements Listener {
    private final ArisTeleport plugin;
    public final Map<UUID, Object> tasks = new HashMap<>();
    private final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private final boolean isFolia;

    public TeleportListener(ArisTeleport plugin) {
        this.plugin = plugin;
        this.isFolia = Bukkit.getVersion().contains("Folia");
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

    private void cancelTask(UUID uuid) {
        Object task = tasks.remove(uuid);
        if (task != null) {
            if (isFolia) {
                ((io.papermc.paper.threadedregions.scheduler.ScheduledTask) task).cancel();
            } else {
                ((org.bukkit.scheduler.BukkitTask) task).cancel();
            }
        }
    }

    public void startTeleport(Player p, Location loc, String type, String warpName) {
        if (tasks.containsKey(p.getUniqueId())) return;
        int delay = plugin.getConfig().getInt("teleport-delay");

        if (isFolia) {
            final int[] tick = {0};
            Object scheduledTask = Bukkit.getRegionScheduler().runAtFixedRate(plugin, p.getLocation(), (task) -> {
                int remaining = delay - tick[0];
                if (remaining > 0) {
                    doNotify(p, type, warpName, remaining);
                    tick[0]++;
                } else {
                    p.teleport(loc);
                    doSuccess(p, type, warpName);
                    cancelTask(p.getUniqueId());
                }
            }, 1L, 20L);
            tasks.put(p.getUniqueId(), scheduledTask);
        } else {
            org.bukkit.scheduler.BukkitTask bukkitTask = new org.bukkit.scheduler.BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (!tasks.containsKey(p.getUniqueId())) { this.cancel(); return; }
                    int remaining = delay - tick;
                    if (remaining > 0) {
                        doNotify(p, type, warpName, remaining);
                        tick++;
                    } else {
                        p.teleport(loc);
                        doSuccess(p, type, warpName);
                        tasks.remove(p.getUniqueId());
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
            tasks.put(p.getUniqueId(), bukkitTask);
        }
    }

    private void doNotify(Player p, String type, String warpName, int time) {
        String raw = plugin.getConfig().getString("messages." + type + ".countdown")
                .replace("%name%", warpName).replace("%time%", String.valueOf(time));
        sendNotify(p, color(raw));
        p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.countdown")), 1.0f, 1.0f);
    }

    private void doSuccess(Player p, String type, String warpName) {
        String rawSuccess = plugin.getConfig().getString("messages." + type + ".success").replace("%name%", warpName);
        sendNotify(p, color(rawSuccess));
        p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.success")), 1.0f, 1.0f);
    }

    private void sendNotify(Player p, String msg) {
        if (plugin.getConfig().getBoolean("settings.use-chat")) p.sendMessage(msg);
        if (plugin.getConfig().getBoolean("settings.use-actionbar")) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
        }
    }

    public String color(String message) {
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString());
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }
          }
