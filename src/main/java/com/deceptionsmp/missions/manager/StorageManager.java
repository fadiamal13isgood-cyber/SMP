package com.deceptionsmp.missions.manager;

import com.deceptionsmp.missions.DeceptionMissions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageManager {

    private final DeceptionMissions plugin;
    private final File file;
    private final FileConfiguration config;
    private final Map<UUID, Integer> xpMap = new HashMap<>();

    public StorageManager(DeceptionMissions plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadAllData();
    }

    public void loadAllData() {
        if (!config.contains("players")) return;
        for (String key : config.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            xpMap.put(uuid, config.getInt("players." + key + ".xp", 0));
        }
    }

    public void saveAllData() {
        for (UUID uuid : xpMap.keySet()) {
            config.set("players." + uuid.toString() + ".xp", getXp(uuid));
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getXp(UUID uuid) { return xpMap.getOrDefault(uuid, 0); }
    public void addXp(UUID uuid, int amt) { xpMap.put(uuid, getXp(uuid) + amt); }
}
