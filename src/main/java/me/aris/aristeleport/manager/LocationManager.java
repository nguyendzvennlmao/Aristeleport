package me.aris.aristeleport.manager;

import me.aris.aristeleport.ArisTeleport;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public class LocationManager {
    private final ArisTeleport plugin;
    private File file;
    private FileConfiguration config;

    public LocationManager(ArisTeleport plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "locations.yml");
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void setLocation(String path, Location loc) {
        config.set(path, loc);
        save();
    }

    public void removeLocation(String path) {
        config.set(path, null);
        save();
    }

    public Location getLocation(String path) {
        return (Location) config.get(path);
    }

    public void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public FileConfiguration getConfig() { return config; }
    }
