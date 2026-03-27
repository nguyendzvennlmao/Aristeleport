package me.aris.aristeleport;

import me.aris.aristeleport.commands.*;
import me.aris.aristeleport.listeners.TeleportListener;
import me.aris.aristeleport.manager.LocationManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ArisTeleport extends JavaPlugin {
    private LocationManager locationManager;
    private TeleportListener teleportListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.locationManager = new LocationManager(this);
        this.teleportListener = new TeleportListener(this);
        getServer().getPluginManager().registerEvents(teleportListener, this);
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("setspawn").setExecutor(new SpawnCommand(this));
        getCommand("delspawn").setExecutor(new SpawnCommand(this));
        getCommand("afk").setExecutor(new AFKCommand(this));
        getCommand("setafk").setExecutor(new AFKCommand(this));
        getCommand("delafk").setExecutor(new AFKCommand(this));
        WarpCommand warpCmd = new WarpCommand(this);
        getCommand("warp").setExecutor(warpCmd);
        getCommand("warp").setTabCompleter(warpCmd);
        getCommand("setwarp").setExecutor(warpCmd);
        getCommand("delwarp").setExecutor(warpCmd);
        getCommand("delwarp").setTabCompleter(warpCmd);
        getCommand("aristeleport").setExecutor(new AdminCommand(this));
    }

    public LocationManager getLocationManager() { return locationManager; }
    public TeleportListener getTeleportListener() { return teleportListener; }
          }
